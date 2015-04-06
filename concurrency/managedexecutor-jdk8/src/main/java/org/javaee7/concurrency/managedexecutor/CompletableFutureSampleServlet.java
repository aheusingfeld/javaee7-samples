package org.javaee7.concurrency.managedexecutor;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * This sample shall show you how to use a ManagedExecutorService as an alternative to the AsyncServlet sample.
 * The use-case is to resolve products by their names (which are hard-coded here for simplicity)
 *
 * The expected outcome when you call the servlet via HTTP GET looks like this
 * <pre>
     End of synchronous doGet()
     Running inside AsyncProductResolver:
     Product{id=-1003761358, name='productA'}
     Product{id=-1003761357, name='productB'}
     Product{id=-1003761356, name='productC'}
     onComplete in AsyncListener
 * </pre>
 * @author Alex Heusingfeld
 */
@WebServlet(urlPatterns = "/CompletableFutureSampleServlet", asyncSupported = true)
public class CompletableFutureSampleServlet extends HttpServlet {

    @Resource(name = "test")
    ManagedExecutorService executor;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AsyncContext ac = request.startAsync();

        ac.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                event.getSuppliedResponse().getWriter().println("onComplete in AsyncListener");
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                event.getSuppliedResponse().getWriter().println("onTimeout in AsyncListener");
                event.getAsyncContext().complete();
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                event.getSuppliedResponse().getWriter().println("onError in AsyncListener");
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                event.getSuppliedResponse().getWriter().println("onStartAsync in AsyncListener");
            }
        });
        executor.submit(new AsyncProductResolver(ac, Arrays.asList("productA", "productB", "productC")));
        response.getWriter().println("End of synchronous doGet()");
    }

    class AsyncProductResolver implements Runnable {

        private final List<String> productNames;
        AsyncContext ac;

        public AsyncProductResolver(AsyncContext ac, final List<String> productNames) {
            this.ac = ac;
            this.productNames = productNames;
        }

        @Override
        public void run() {
            try {
                PrintWriter writer = ac.getResponse().getWriter();
                writer.println("Running inside AsyncProductResolver: ");
                productNames.stream()
                        .map(this::resolveProductByName)
                        .forEach(writer::println);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            ac.complete();
        }

        /**
         * This method shall retrieve the product information e.g. from a backend application.
         * @param productName the name to resolve
         * @return the product returned
         */
        private Product resolveProductByName(String productName) {
            // For reasons of simplicity we skip the backend call here
            return new Product(productName.hashCode(), productName);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
// </editor-fold>
}

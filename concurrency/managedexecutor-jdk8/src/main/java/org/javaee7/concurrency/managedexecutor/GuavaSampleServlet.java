package org.javaee7.concurrency.managedexecutor;

import com.google.common.util.concurrent.*;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Unfortunately {@link javax.enterprise.concurrent.ManagedExecutorService} still only returns JDK5 Futures. If you 
 * don't have Java8 available, you might want to try Google Guava's ListenableFuture to be able to chain/ add callbacks
 * to react upon async results. 
 */
@WebServlet(urlPatterns = "/GuavaSampleServlet", asyncSupported = true)
public class GuavaSampleServlet extends HttpServlet {

    @Resource(name = "DefaultManagedExecutorService")
    ManagedExecutorService executor;
    private Product product;

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ListeningExecutorService service = MoreExecutors.listeningDecorator(executor);
        ListenableFuture<Product> explosion = service.submit(() -> shipProduct());

        Futures.addCallback(explosion, new FutureCallback<Product>() {
            // we want this handler to run immediately after we push the big red button!     
            public void onSuccess(Product product) {
                System.out.println("Product shipped");
            }

            public void onFailure(Throwable thrown) {
                System.out.println("Product shipment failed!");
            }
        });
    }

    public Product shipProduct() {
        return new Product(12345);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    //</editor-fold>
}

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) {

        NioSocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("LineCodec", new ProtocolCodecFilter(new TextLineCodecFactory()));
        acceptor.setHandler(new TClientHandler());
        try {
            acceptor.bind(new InetSocketAddress(55555));
            System.out.println("Server started at port 55555");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

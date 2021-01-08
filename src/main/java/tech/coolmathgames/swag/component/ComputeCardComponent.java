package tech.coolmathgames.swag.component;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractValue;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ComputeCardComponent extends BaseComponent {
  public final Set<DgramSocket> connections = new HashSet<DgramSocket>();
  public final Set<TCPServer> tcpConnections = new HashSet<TCPServer>();

  public ComputeCardComponent(EnvironmentHost host) {
    super("compute_card", host);
    node = Network.newNode(this, Visibility.Network).withComponent(getComponentName()).withConnector(32).create();
  }

  @Callback(doc = "function():number; Gets the current real-world time in seconds since the Unix epoch")
  public Object[] time(Context context, Arguments args) {
    return new Object[] {System.currentTimeMillis() / 1000.0};
  }

  @Callback(doc = "function():userdata -- Creates a Dgram socket")
  public Object[] openDgram(Context context, Arguments arguments) throws IOException {
    if(connections.size() > 5) {
      throw new IOException("Too many Dgram sockets open!");
    }
    DgramSocket socket = new DgramSocket(this);
    connections.add(socket);
    return new Object[] { socket };
  }

  @Callback(doc = "function(port:number):userdata -- Binds to a TCP port")
  public Object[] bind(Context context, Arguments arguments) throws IOException {
    if(tcpConnections.size() > 5) {
      throw new IOException("Too many TCP ports bound!");
    }
    TCPServer socket = new TCPServer(this, arguments.checkInteger(0));
    tcpConnections.add(socket);
    return new Object[] { socket };
  }

  public static class TCPSocket extends AbstractValue {
    private SocketChannel socket;
    private ComputeCardComponent component;
    private TCPServer server;

    @SuppressWarnings("unused")
    public TCPSocket() {
      super();
      this.socket = null;
      this.server = null;
      this.component = null;
    }

    public TCPSocket(ComputeCardComponent component, TCPServer server, SocketChannel socket) throws IOException {
      super();
      this.socket = socket;
      this.server = server;
      this.component = component;
      this.socket.configureBlocking(false);
    }

    @Callback(doc = "function(amount:number):string -- Reads specified number of bytes into a string. Returns empty string if not enough queued bytes")
    public Object[] read(Context context, Arguments arguments) throws IOException {
      if(this.socket == null || !this.socket.isOpen()) {
        throw new IOException("socket was closed");
      }

      int length = arguments.checkInteger(0);
      ByteBuffer buffer = ByteBuffer.allocate(length);
      int offset = 0;
      while(offset < length) {
        long read = this.socket.read(buffer);
        offset += read;
        // Out of bytes to read
        if(read < 16384) {
          break;
        }
      }
      return new Object[] {new String(buffer.array(), 0, offset, StandardCharsets.UTF_8)};
    }

    @Callback(doc = "function(data:string) -- Write bytes to socket")
    public Object[] write(Context context, Arguments arguments) throws IOException {
      if(this.socket == null || !this.socket.isOpen()) {
        throw new IOException("socket was closed");
      }

      ByteBuffer byteBuffer = ByteBuffer.wrap(arguments.checkByteArray(0));
      // For some reason OpenJDK doesn't write all at once?
      while(byteBuffer.hasRemaining() && this.socket.write(byteBuffer) != 0);
      return new Object[] {null};
    }

    @Callback(doc = "function():string, number, string -- Gets remote address, port, and canonical hostname")
    public Object[] getAddress(Context context, Arguments argument) throws IOException {
      if(this.socket == null || !this.socket.isOpen()) {
        throw new IOException("socket was closed");
      }
      InetSocketAddress socketAddress = (InetSocketAddress) this.socket.getRemoteAddress();
      if(socketAddress == null) {
        return null;
      }
      return new Object[] {
        socketAddress.getAddress().getHostAddress(),
        socketAddress.getPort(),
        socketAddress.getAddress().getCanonicalHostName()
      };
    }

    @Callback(doc = "function() -- Close")
    public Object[] close(Context context, Arguments arguments) {
      this.close();
      return new Object[] {null};
    }

    @Override
    public void dispose(Context context) {
      super.dispose(context);
      this.close();
    }

    private void close() {
      if(this.socket != null && this.socket.isOpen()) {
        try {
          this.socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      this.socket = null;
      this.server = null;
      this.component = null;
    }
  }

  public static class TCPServer extends AbstractValue {
    private ComputeCardComponent component;
    private ServerSocketChannel socket;

    // Userdata
    @SuppressWarnings("unused")
    public TCPServer() {
      super();
      this.socket = null;
      this.component = null;
    }

    public TCPServer(ComputeCardComponent component, int port) throws IOException {
      super();
      this.socket = ServerSocketChannel.open();
      this.socket.bind(new InetSocketAddress(Inet4Address.getByAddress(new byte[]{0,0,0,0}), port));
      this.socket.configureBlocking(false);
      this.component = component;
    }

    @Callback(doc = "function():userdata -- Returns TCP client if available")
    public Object[] accept(Context context, Arguments arguments) throws IOException {
      if(this.socket == null || !this.socket.isOpen()) {
        throw new IOException("server was closed");
      }
      SocketChannel client = this.socket.accept();
      if(client == null) {
        return null;
      }
      TCPSocket socket = new TCPSocket(this.component, this, client);
      return new Object[] { socket };
    }

    @Callback(doc = "function() -- Closes socket")
    public Object[] close(Context context, Arguments arguments) {
      if(this.socket != null && this.socket.isOpen()) {
        this.close();
      }
      return new Object[] {null};
    }

    @Override
    public void dispose(Context context) {
      super.dispose(context);
      this.close();
    }

    private void close() {
      if(this.socket != null) {
        try {
          this.socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if(this.component != null) {
        component.tcpConnections.remove(this);
      }
      this.component = null;
      this.socket = null;
    }
  }

  public static class DgramSocket extends AbstractValue {
    private ComputeCardComponent component;
    private DatagramChannel socket;

    // Userdata
    @SuppressWarnings("unused")
    public DgramSocket() {
      super();
      this.socket = null;
      this.component = null;
    }

    public DgramSocket(ComputeCardComponent component) throws IOException {
      super();
      this.socket = DatagramChannel.open(StandardProtocolFamily.INET);
      this.socket.bind(null);
      this.socket.configureBlocking(false);
      this.component = component;
    }

    @Callback(doc = "function():string, number, string -- Returns ip, port, and data of received packet")
    public Object[] read(Context context, Arguments arguments) throws IOException {
      if(this.socket == null) {
        throw new IOException("socket was disconnected");
      }
      ByteBuffer buffer = ByteBuffer.allocate(1024*1024);
      // No idea why this isn't already InetSocketAddress...
      InetSocketAddress socketAddress = (InetSocketAddress) socket.receive(buffer);
      if(socketAddress == null) {
        return null;
      }
      String received = new String(buffer.array(), 0, buffer.position());
      return new Object[] {socketAddress.getAddress(), socketAddress.getPort(), received};
    }

    @Callback(doc = "function(address:string, port:number, data:string) -- Sends packet to specified address")
    public Object[] write(Context context, Arguments arguments) throws IOException {
      if(this.socket == null) {
        throw new IOException("socket was disconnected");
      }
      byte[] buf = arguments.checkString(2).getBytes();
      this.socket.send(ByteBuffer.wrap(buf), new InetSocketAddress(arguments.checkString(0), arguments.checkInteger(1)));
      return null;
    }

    @Callback(doc = "function() -- Closes the socket")
    public Object[] close(Context context, Arguments arguments) {
      this.close();
      return null;
    }

    @Override
    public void dispose(Context context) {
      super.dispose(context);
      this.close();
    }

    private void close() {
      if(this.socket != null) {
        try {
          this.socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if(this.component != null) {
        component.connections.remove(this);
      }
      this.component = null;
      this.socket = null;
    }
  }
}

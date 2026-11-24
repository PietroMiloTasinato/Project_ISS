package webserver;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;
import unibo.basicomm23.interfaces.IApplMessage;
import unibo.basicomm23.interfaces.IObserver;
import unibo.basicomm23.interfaces.Interaction;
import unibo.basicomm23.msg.ApplMessage;
import unibo.basicomm23.msg.ProtocolType;
import unibo.basicomm23.utils.CommUtils;
import unibo.basicomm23.utils.Connection;
import unibo.basicomm23.utils.ConnectionFactory;
import unibo.basicomm23.ws.WsConnection;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class WebServer {

	private static Javalin webServer = null;
	private static final String requestToLoad = "request_to_load";
	private static final Map<Session, WsConnectContext> webGuiCtxMap = new HashMap<>();
	private static volatile Interaction tcpIoPortConn = null;

	public static void setUpWebServer() throws Exception {
		if (webServer == null) {
			webServer = Javalin.create(javalinConfig -> {
				javalinConfig.jetty.modifyWebSocketServletFactory(factory ->
						factory.setIdleTimeout(Duration.ZERO));
				javalinConfig.staticFiles.add(staticFileConfig -> {
					staticFileConfig.directory = "/webgui";
					staticFileConfig.location = Location.CLASSPATH;
				});
			}).start(8010);
		}
		webServer.get("/", ctx -> {
			try(InputStream inputStream = WebServer.class.getResourceAsStream("/webgui/webgui.html")) {
				if (inputStream != null) {
					String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
					ctx.html(content);
				} else {
					ctx.status(404).result("File not found.");
				}
			} catch(Exception e){
				ctx.status(404).result("File not found.");
			}
		});
		webServer.ws("/webgui", ws -> {
			ws.onConnect( ctx -> {
				webGuiCtxMap.put(ctx.session, ctx);
				CommUtils.outgreen("server: connection established");
			});
			ws.onMessage(ctx -> {
				String message = ctx.message();
				if(message.equals(requestToLoad) && tcpIoPortConn != null){
					IApplMessage dispatch = CommUtils.buildDispatch("webgui", "press_button", "press_button(0)", "io_port");
					tcpIoPortConn.forward(dispatch);
				}
				CommUtils.outcyan("Message from webgui: " + message);
			});
			ws.onClose(ctx -> {
				webGuiCtxMap.remove(ctx.session);
				CommUtils.outcyan("server: connection closed");
			});
		});
		webServer.ws("/ioport", ws -> {
			ws.onConnect(ctx -> {
				CommUtils.outcyan("io port linked: " + ctx);
			});
			ws.onMessage(ctx -> {
				String rawMessage = ctx.message();
				try {
					IApplMessage message = ApplMessage.buildFromStruct(rawMessage);
					String jsonMessage = handleIoPortMessage(message);
					for (WsConnectContext webGuiCtx : webGuiCtxMap.values()) {
						webGuiCtx.send(jsonMessage);
					}
				} catch (Exception e) {
					CommUtils.outcyan("Mesage structure incorrect: " + rawMessage);
                }
			});
			ws.onClose(ctx -> {
				CommUtils.outcyan("Io port connection closed.");
				tcpIoPortConn = null;
				startPollingIoPort();
			});
		});
		startPollingIoPort();
	}

	private static void startPollingIoPort(){
		new Thread(() -> {
			try {
				while (tcpIoPortConn == null) {
					tcpIoPortConn = ConnectionFactory.createClientSupport23(ProtocolType.tcp, "localhost", "8000");
					Thread.sleep(2000);
				}
				IApplMessage dispatch = CommUtils.buildDispatch("webgui", "link_webgui", "link_webgui(0)", "io_port");
				tcpIoPortConn.forward(dispatch);
			} catch (Exception e) {
				CommUtils.outyellow("Io port scanning thread crashed: " + e);
			}
		}).start();
	}

	private static String handleIoPortMessage(IApplMessage message) throws IllegalArgumentException{
		JSONObject obj = new JSONObject();
		switch(message.msgId()){
			case "accepted" -> {
				obj.put("state", "Accepted");
				obj.put("slot", message.msgContent());
			}
			case "rejected" -> {
				obj.put("state", "Rejected");
				obj.put("slot", "No slot assigned");
			}
			case "retrylater" -> {
				obj.put("state", "Retry later");
				obj.put("slot", "No slot assigned");
			}
            default -> throw new IllegalArgumentException();
		}
		return obj.toString();
	}
	
	public static void main(String[] args) throws Exception {
		setUpWebServer();
	}
}

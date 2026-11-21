package webserver;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import unibo.basicomm23.utils.CommUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class WebServer {

	private static Javalin webServer = null;
	
	public static void setUpWebServer() {
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
					ctx.status(404).result("File non trovato nel file system");
				}
			} catch(Exception e){
				ctx.status(404).result("File non trovato nel file system");
			}
		});
		webServer.ws("/webgui", 
			ws -> {
				ws.onConnect( ctx -> { 
					String ss = "{\"msg\" : \"welcome\" }";
				    ctx.send( ss  );
					CommUtils.outgreen("server: connection established");
				});
				ws.onMessage(ctx -> {
					String message = ctx.message();

				 	CommUtils.outcyan("Message from webgui: " + message);
				});
				ws.onClose(ctx -> {
					System.out.println("server: connection closed"); 
				});
			});
	}
	
	public static void main(String[] args) {
		setUpWebServer();
	}
}

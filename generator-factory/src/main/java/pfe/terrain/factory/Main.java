package pfe.terrain.factory;

import com.google.gson.Gson;
import pfe.terrain.factory.controller.ServiceController;
import pfe.terrain.factory.parser.JsonCompoParser;
import pfe.terrain.factory.parser.JsonParser;

import java.util.List;

import static spark.Spark.*;


public class Main {
    public static void main(String[] args) {
        ServiceController controller = new ServiceController();
        JsonParser parser = new JsonParser();

        port(9090);

        get("/algorithms", (request, response) -> {
            response.type("application/json");

            try{
                return parser.algoListToJson(controller.getAlgoList());
            }catch (Exception e){
                response.status(500);
                return parser.exceptionToJson(e);
            }
        });

        post("/generator", (request,response) -> {
            response.type("application/xml");

            try{
                return controller.getGenerator(parser.listFromJson(request.body())).toString();
            } catch (Exception e){
                response.status(500);
                return parser.exceptionToJson(e);
            }
        });

        get("/compositions", (request,response) -> {
            response.type("application/json");

            try{
                return parser.compoToJson(controller.getCompositions());
            } catch (Exception e){
                response.status(500);
                return parser.exceptionToJson(e);
            }
        });

        post("/compositions", (request,response) -> {
            response.type("application/json");

            try{
                JsonCompoParser compoParser = new JsonCompoParser(request.body());
                controller.addComposition(compoParser.getName(),compoParser.getAlgoName(),compoParser.getContext());
                return parser.okAnswer();
            }catch (Exception e){

                response.status(500);
                return parser.exceptionToJson(e);
            }
        });

        get("/compositions/:compoName/pom", (request,response) -> {
            response.type("application/xml");
            try{
                String name = request.params(":compoName");
                return controller.getCompositionPom(name);

            }catch (Exception e){
                response.type("application/json");
                response.status(500);
                return parser.exceptionToJson(e);
            }
        });

        get("/compositions/:compoName/context", (request,response) -> {
            response.type("application/json");

            try{
                String name = request.params(":compoName");
                return controller.getCompositionContext(name);
            }catch (Exception e){
                response.status(500);
                return parser.exceptionToJson(e);
            }
        });

        delete("/compositions/:compoName", (request, response) -> {
            response.type("application/json");

            try{
                String name = request.params(":compoName");
                controller.deleteComposition(name);
                return parser.okAnswer();
            }catch (Exception e){
                response.status(500);
                return parser.exceptionToJson(e);
            }
        });


    }
}

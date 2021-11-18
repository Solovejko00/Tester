import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

public class UserTest {
    String url = "http://localhost:3000/test";

    @Test // тест на парсинг json файла
    public void readJsonTest(){
        Gson gson = new Gson();
        String testString = """
                {
                  "method": "POST",
                  "countRequest": 10,
                  "input": {
                    "User": "Bob",
                    "Password": "123456",
                    "Numbers": [
                      "8928329319231",
                      "8927752421423"
                    ]
                  },
                  "responseCodes": [
                    150,
                    200
                  ],
                  "checkOutBody": false,
                  "output": {
                    "User": "Bob",
                    "Password": "123456",
                    "Numbers": [
                      "8928329319231",
                      "8927752421423"
                    ]
                  },
                  "timeOut": 10000,
                  "thread": 5,
                  "intervalRequest": 0,
                  "intervalOnThread": 0,
                  "url": "http://localhost:3000/test",
                  "log": "INFO"
                }""";

        File file = new File("test.json");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.print(testString);
        pw.close();

        String[] args = new String[2];
        args[0] = url;
        args[1] = "test.json";

        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < testString.length(); i++){
            if ((testString.charAt(i) != '\n') && (testString.charAt(i) != ' ')){
                expected.append(testString.charAt(i));
            }
        }

        Main.Setting setting = new Main.Setting(Main.readJson(args));
        Assert.assertEquals(expected.toString(), gson.toJson(setting));
    }

    @Test // GET стандартный
    public void sendRequestTest0() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(150);
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("OK", response.status);
    }

    @Test // GET код ответа Integer.MAX_VALUE
    public void sendRequestTest1_1() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(Integer.MAX_VALUE);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("ERROR", response.status);
    }

    @Test // GET код ответа Integer.MIN_VALUE
    public void sendRequestTest1_2() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(Integer.MIN_VALUE);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("ERROR", response.status);
    }

    @Test // GET не указываем коды ответа
    public void sendRequestTest2() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.responseCodes = new ArrayList<Integer>();
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("ERROR", response.status);
    }

    @Test // GET проверяем тело ответа верное
    public void sendRequestTest3() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = true;
        setting.output = new JsonObject();
        setting.output.addProperty("Status", "OK");
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("OK", response.status);
    }

    @Test // GET проверяем тело ответа неверное
    public void sendRequestTest4() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = true;
        setting.output = new JsonObject();
        setting.output.addProperty("Status", "qweqeqwe");
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("ERROR", response.status);
    }

    @Test // POST запрос стандартный
    public void sendRequestTest5() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "POST";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.input = new JsonObject();
        setting.input.addProperty("Name", "Bob");
        setting.input.addProperty("Password", "123");
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("OK", response.status);
    }

    @Test // POST запрос проверяем тело ответа верное
    public void sendRequestTest6() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "POST";
        setting.log = "INFO";
        setting.checkOutBody = true;
        setting.input = new JsonObject();
        setting.input.addProperty("Name", "Bob");
        setting.input.addProperty("Password", "123");
        setting.output = new JsonObject();
        setting.output.addProperty("Name", "Bob");
        setting.output.addProperty("Password", "123");
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("OK", response.status);
    }

    @Test // POST запрос проверяем тело ответа неверное
    public void sendRequestTest7() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "POST";
        setting.log = "INFO";
        setting.checkOutBody = true;
        setting.input = new JsonObject();
        setting.input.addProperty("Name", "Bob");
        setting.input.addProperty("Password", "123");
        setting.output = new JsonObject();
        setting.output.addProperty("Name", "qwerqwer");
        setting.output.addProperty("Password", "qwerqer");
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("ERROR", response.status);
    }

    @Test // DELETE запрос стандартный
    public void sendRequestTest8() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "POST";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.input = new JsonObject();
        setting.input.addProperty("Name", "Bob");
        setting.input.addProperty("Password", "123");
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("OK", response.status);
    }

    @Test // DELETE запрос проверяем тело ответа верное
    public void sendRequestTest9() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "DELETE";
        setting.log = "INFO";
        setting.checkOutBody = true;
        setting.input = new JsonObject();
        setting.input.addProperty("Name", "Bob");
        setting.input.addProperty("Password", "123");
        setting.output = new JsonObject();
        setting.output.addProperty("Name", "Bob");
        setting.output.addProperty("Password", "123");
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("OK", response.status);
    }

    @Test // DELETE запрос проверяем тело ответа неверное
    public void sendRequestTest10() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "DELETE";
        setting.log = "INFO";
        setting.checkOutBody = true;
        setting.input = new JsonObject();
        setting.input.addProperty("Name", "Bob");
        setting.input.addProperty("Password", "123");
        setting.output = new JsonObject();
        setting.output.addProperty("Name", "qwerqwer");
        setting.output.addProperty("Password", "qwerqer");
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("ERROR", response.status);
    }

    @Test // PUT запрос стандартный
    public void sendRequestTest11() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "PUT";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.input = new JsonObject();
        setting.input.addProperty("Name", "Bob");
        setting.input.addProperty("Password", "123");
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("OK", response.status);
    }

    @Test // PUT запрос проверяем тело ответа верное
    public void sendRequestTest12() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "PUT";
        setting.log = "INFO";
        setting.checkOutBody = true;
        setting.input = new JsonObject();
        setting.input.addProperty("Name", "Bob");
        setting.input.addProperty("Password", "123");
        setting.output = new JsonObject();
        setting.output.addProperty("Name", "Bob");
        setting.output.addProperty("Password", "123");
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);

        Assert.assertEquals("OK", response.status);
    }

    @Test // PUT запрос проверяем тело ответа неверное
    public void sendRequestTest13() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "PUT";
        setting.log = "INFO";
        setting.checkOutBody = true;
        setting.input = new JsonObject();
        setting.input.addProperty("Name", "Bob");
        setting.input.addProperty("Password", "123");
        setting.output = new JsonObject();
        setting.output.addProperty("Name", "qwerqwer");
        setting.output.addProperty("Password", "qwerqer");
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);

        Main.Response response = Main.sendRequest(setting, statistics);
        Assert.assertEquals("ERROR", response.status);
    }

    @Test // countRequest -1
    public void spamTest1() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = true;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);
        setting.intervalRequest = 0;
        setting.countRequest = -1;

        Main.spam(setting, statistics);

        Assert.assertEquals(0, statistics.countReq);
    }

    @Test // countRequest 0
    public void spamTest2() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = true;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);
        setting.intervalRequest = 0;
        setting.countRequest = 0;

        Main.spam(setting, statistics);

        Assert.assertEquals(0, statistics.countReq);
    }

    @Test // countRequest 1
    public void spamTest3() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);
        setting.intervalRequest = 0;
        setting.countRequest = 1;

        Main.spam(setting, statistics);

        Assert.assertEquals(1, statistics.countReq);
    }

    @Test // countRequest Integer.MIN_VALUE
    public void spamTest5() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);
        setting.intervalRequest = 0;
        setting.countRequest = Integer.MIN_VALUE;

        Main.spam(setting, statistics);

        Assert.assertEquals(0, statistics.countReq);
    }

    @Test // intervalRequest -1
    public void spamTest6() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);
        setting.intervalRequest = -1;
        setting.countRequest = Integer.MIN_VALUE;

        Main.spam(setting, statistics);

        Assert.assertEquals(0, statistics.countReq);
    }

    @Test // thread -1
    public void startThreads1() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);
        setting.intervalRequest = 0;
        setting.intervalOnThread = 0;
        setting.countRequest = 1;
        setting.thread = -1;

        Main.startThreads(setting, statistics);

        Assert.assertEquals(0, statistics.threads);
    }

    @Test // thread 0
    public void startThreads2() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);
        setting.intervalRequest = 0;
        setting.intervalOnThread = 0;
        setting.countRequest = 1;
        setting.thread = 0;

        Main.startThreads(setting, statistics);

        Assert.assertEquals(0, statistics.threads);
    }

    @Test // thread 1
    public void startThreads3() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);
        setting.intervalRequest = 0;
        setting.intervalOnThread = 0;
        setting.countRequest = 1;
        setting.thread = 1;

        Main.startThreads(setting, statistics);

        Assert.assertEquals(1, statistics.threads);
    }

    @Test // thread Integer.MIN_VALUE
    public void startThreads4() throws IOException {
        Main.Setting setting = new Main.Setting();
        Main.Statistics statistics = new Main.Statistics();
        setting.method = "GET";
        setting.log = "INFO";
        setting.checkOutBody = false;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.timeOut = 0;
        setting.url = new URL(url);
        setting.intervalRequest = 0;
        setting.intervalOnThread = 0;
        setting.countRequest = 1;
        setting.thread = Integer.MIN_VALUE;

        Main.startThreads(setting, statistics);

        Assert.assertEquals(0, statistics.threads);
    }

    @Test // стандартный
    public void mainTest1() throws IOException {
        Gson gson = new Gson();
        Main.Setting setting = new Main.Setting();
        setting.method = "GET";
        setting.countRequest = 10;
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.checkOutBody = false;
        setting.timeOut = 0;
        setting.thread = 5;
        setting.intervalRequest = 0;
        setting.intervalOnThread = 0;

        File file = new File("test1.json");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.print(gson.toJson(setting));
        pw.close();

        String[] args = new String[2];
        args[0] = url;
        args[1] = "test1.json";
        Main.main(args);
    }

    @Test // POST запрос с телом
    public void mainTest2() throws IOException {
        Gson gson = new Gson();
        Main.Setting setting = new Main.Setting();
        setting.method = "POST";
        setting.countRequest = 1;
        setting.input = new JsonObject();
        setting.input.addProperty("ID", 132);
        JsonObject user = new JsonObject();
        user.addProperty("Name", "Jon");
        user.addProperty("Number", 89123129);
        setting.input.add("USER", user);
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.checkOutBody = false;
        setting.timeOut = 0;
        setting.thread = 100;
        setting.intervalRequest = 0;
        setting.intervalOnThread = 0;

        File file = new File("test2.json");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.print(gson.toJson(setting));
        pw.close();

        String[] args = new String[2];
        args[0] = url;
        args[1] = "test2.json";
        Main.main(args);
    }

    @Test // POST запрос с телом и проверкой ответа
    public void mainTest3() throws IOException {
        Gson gson = new Gson();
        Main.Setting setting = new Main.Setting();
        setting.method = "POST";
        setting.countRequest = 100;
        setting.input = new JsonObject();
        setting.input.addProperty("ID", 132);
        JsonObject user = new JsonObject();
        user.addProperty("Name", "Jon");
        user.addProperty("Number", 89123129);
        setting.input.add("USER", user);
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.responseCodes.add(200);
        setting.checkOutBody = true;
        setting.output = new JsonObject();
        setting.output.addProperty("ID", 132);
        setting.output.add("USER", user);
        setting.timeOut = 0;
        setting.thread = 1;
        setting.intervalRequest = 0;
        setting.intervalOnThread = 0;

        File file = new File("test3.json");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.print(gson.toJson(setting));
        pw.close();

        String[] args = new String[2];
        args[0] = url;
        args[1] = "test3.json";
        Main.main(args);
    }

    @Test // GET с интервалами
    public void mainTest4() throws IOException {
        Gson gson = new Gson();
        Main.Setting setting = new Main.Setting();
        setting.method = "GET";
        setting.countRequest = 10;
        setting.input = new JsonObject();
        setting.input.addProperty("ID", 132);
        JsonObject user = new JsonObject();
        user.addProperty("Name", "Jon");
        user.addProperty("Number", 89123129);
        setting.input.add("USER", user);
        setting.responseCodes = new ArrayList<Integer>();
        setting.responseCodes.add(200);
        setting.checkOutBody = false;
        setting.timeOut = 0;
        setting.thread = 10;
        setting.intervalRequest = 2;
        setting.intervalOnThread = 2;

        File file = new File("test4.json");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.print(gson.toJson(setting));
        pw.close();

        String[] args = new String[2];
        args[0] = url;
        args[1] = "test4.json";
        Main.main(args);
    }

    @Test // PUT без кодов ответа
    public void mainTest5() throws IOException {
        Gson gson = new Gson();
        Main.Setting setting = new Main.Setting();
        setting.method = "PUT";
        setting.countRequest = 10;
        setting.input = new JsonObject();
        setting.input.addProperty("ID", 132);
        JsonObject user = new JsonObject();
        user.addProperty("Name", "Jon");
        user.addProperty("Number", 89123129);
        setting.input.add("USER", user);
        setting.responseCodes = new ArrayList<Integer>();
        setting.checkOutBody = false;
        setting.timeOut = 0;
        setting.thread = 5;
        setting.intervalRequest = 0;
        setting.intervalOnThread = 0;

        File file = new File("test5.json");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.print(gson.toJson(setting));
        pw.close();

        String[] args = new String[2];
        args[0] = url;
        args[1] = "test5.json";
        Main.main(args);
    }

}

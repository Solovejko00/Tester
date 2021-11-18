import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.gson.JsonSyntaxException;
import org.apache.log4j.Logger;

public class Main {
    static class Setting{
        public String method;
        public String endPoint;
        public Integer countRequest;
        public JsonObject input;
        public List<Integer> responseCodes;
        public boolean checkOutBody;
        public JsonObject output;
        public Integer timeOut;
        public Integer thread;
        public Integer intervalRequest;
        public Integer intervalOnThread;
        public URL url;
        public String log;

        public Setting(Setting set) {
            this.method = set.method;
            this.countRequest = set.countRequest;
            this.input = set.input;
            this.responseCodes = new ArrayList<>(set.responseCodes);
            this.checkOutBody = set.checkOutBody;
            this.output = set.output;
            this.timeOut = set.timeOut;
            this.thread = set.thread;
            this.intervalRequest = set.intervalRequest;
            this.intervalOnThread = set.intervalOnThread;
            this.url = set.url;
            this.log = set.log;
        }

        public Setting() {

        }
    }
    static class Response{
        public Instant firstPoint;
        public Instant secondPoint;
        public Date date1, date2;
        public long delay;
        public String status;
        public long sizeReq;
        public long sizeRes;

        public Response(){
            sizeReq = 0;
            sizeRes = 0;
        }
    }
    static class Statistics{
        public long sumDelay = 0;
        public long countError = 0;
        public double kb = 0.0d;
        public long countReq = 0;

        public double requestsPerSeconds;
        public double averageDelay;
        public double errors;
        public long threads = 0;
        public long maxDelay = 0;
        public double capacity;
    }

    static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args){
        logger.info("Start");
        Setting setting = new Setting(readJson(args));
        Statistics statistics = new Statistics();
        startThreads(setting, statistics);
        printStatistics(statistics);
        logger.info("Finish");
    }

    public static Setting readJson(String[] args){
        if (args.length != 2){
            logger.error("Incorrect number of arguments");
            logger.info("Finish");
            System.exit(1);
        }

        if ((System.getenv("log") != null) && System.getenv("log").equals("DEBUG")) {
            logger.info("Starting function readJson");
        }

        Setting setting = new Setting();

        Gson gson = new Gson();
        try {
            setting = gson.fromJson(new FileReader(args[1]), Setting.class);
            setting.url = new URL(args[0]);
        } catch (MalformedURLException | FileNotFoundException | JsonSyntaxException e) {
            logger.error(e);
            logger.info("Finish");
            System.exit(1);
        }

        setting.log = System.getenv("log");
        if (setting.log == null)
            setting.log = "INFO";

        if (setting.log.equals("INFO")){
            logger.info(args[1] + " " + setting.url);
        }

        if (setting.log.equals("DEBUG")){
            logger.info("\n      * " + args[1] + " " + setting.url + '\n' +
                    "      * method: " + setting.method + '\n' +
                    "      * countRequest: " + setting.countRequest + '\n' +
                    "      * input: " + setting.input + '\n' +
                    "      * responseCodes: " + setting.responseCodes + '\n' +
                    "      * checkOutBody: " + setting.checkOutBody + '\n' +
                    "      * output: " + setting.output + '\n' +
                    "      * timeOut: " + setting.timeOut + '\n' +
                    "      * thread: " + setting.thread + '\n' +
                    "      * intervalRequest: " + setting.intervalRequest + '\n' +
                    "      * intervalOnThread: " + setting.intervalOnThread);
        }

        if (setting.log.equals("DEBUG")) {
            logger.info("Closing function readJson");
        }
        return setting;
    }

    public static void startThreads(Setting setting, Statistics statistics){
        if (setting.log.equals("DEBUG")) {
            logger.info("Starting function startThreads");
        }
        System.out.format("| %8s | %8s | %6s | %6s | %8s | %8s |\n", "Date req", "Date res", "Delay", "Status", "Size req", "Size res");
        for (int i = 0; i < 63; i++) {System.out.print('-');};
        System.out.println();
        Instant startPoint = Instant.now();
        try {
            List<MyThread> list = new ArrayList<>();
            for (int i = 0; i < setting.thread; i++){
                MyThread thread = new MyThread(setting, statistics);
                thread.start();
                list.add(thread);
                TimeUnit.SECONDS.sleep(setting.intervalOnThread);
            }
            for (MyThread thread: list){
                thread.join();
            }
        } catch (InterruptedException e) {
            logger.error(e);
        }
        Instant finishPoint = Instant.now();
        for (int i = 0; i < 63; i++) {System.out.print('-');};
        System.out.println();

        statistics.requestsPerSeconds = (double) statistics.countReq / (double) Duration.between(startPoint, finishPoint).toMillis() * 1000;
        statistics.averageDelay = (double) statistics.sumDelay / (double) statistics.countReq;
        statistics.errors = (double) statistics.countError / (double) statistics.countReq * 100;
        statistics.capacity = statistics.kb / (double) statistics.countReq;
        if (setting.log.equals("DEBUG")) {
            logger.info("Closing function startThreads");
        }
    }

    public static Response sendRequest(Setting setting, Statistics statistics) throws IOException{
        if (setting.log.equals("DEBUG")){
            logger.info("Thread: " + Thread.currentThread().getName() + " Starting function sendRequest()");
        }
        statistics.countReq++;
        Response response = new Response();
        HttpURLConnection con;
        con = (HttpURLConnection) setting.url.openConnection();
        con.setRequestMethod(setting.method);
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(setting.timeOut);
        con.setDoInput(true);
        con.setDoOutput(true);
        con.connect();

        if (!setting.method.equals("GET")) { // if we must send body
            byte[] out = setting.input.toString().getBytes();
            OutputStream output = con.getOutputStream();
            output.write(out);
            output.close();
            response.sizeReq = out.length;
        }

        response.firstPoint = Instant.now();
        response.date1 = new Date();
        int code = con.getResponseCode();
        response.secondPoint = Instant.now();
        response.date2 = new Date();

        response.delay = Duration.between(response.firstPoint, response.secondPoint).toMillis();

        response.status = "OK";
        StringBuilder urlString = new StringBuilder();
        boolean findCode = false;

        for (Integer integer: setting.responseCodes)
            if (integer == code) {
                findCode = true;
                break;
            }

        if (findCode){
            BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while((line = input.readLine()) != null) {
                urlString.append(line);
            }
            if (setting.checkOutBody){
                if (!urlString.toString().equals(setting.output.toString())){
                    response.status = "ERROR";
                }
            }
            input.close();
        } else {
            response.status = "ERROR";
        }
        response.sizeRes = con.getContentLengthLong();
        if (response.sizeRes < 0){
            response.sizeRes = 0;
        }

        con.disconnect();

        if (setting.log.equals("DEBUG")){
            logger.info("Thread: " + Thread.currentThread().getName() + " Closing function sendRequest()");
        }
        return response;
    }

    static class MyThread extends Thread {
        Setting setting;
        Statistics statistics;
        MyThread(Setting setting, Statistics statistics){
            this.setting = new Setting(setting);
            this.statistics = statistics;
        }
        @Override
        public void run(){
            statistics.threads++;
            spam(setting, statistics);
        }
    }

    public static void spam(Setting setting, Statistics statistics) {
        if (setting.log.equals("DEBUG")){
            logger.info("Thread: " + Thread.currentThread().getName() + " Starting function spam()");
        }
        for (int i = 0; i < setting.countRequest; i++){
            Response response;
            try {
                response = sendRequest(setting, statistics);
            } catch (IOException e) {
                logger.error("Thread: " + Thread.currentThread().getName() + " " + e);
                statistics.countError++;
                continue;
            }
            if (response.status.equals("ERROR")){
                statistics.countError++;
            }
            statistics.sumDelay += response.delay;
            if (response.delay > statistics.maxDelay)
                statistics.maxDelay = response.delay;
            statistics.kb += ((double)response.sizeReq + (double)response.sizeRes) / (double)response.delay * 1000.0d / 1024.0d;
            DateFormat fr = new SimpleDateFormat("kk:mm:ss");

            System.out.format("| %8s | %8s | %6s | %6s | %8s | %8s |\n", fr.format(response.date1), fr.format(response.date2), response.delay,
                    response.status, response.sizeReq, response.sizeRes);

            try {
                TimeUnit.SECONDS.sleep(setting.intervalRequest);
            } catch (InterruptedException e) {
                logger.error("Thread: " + Thread.currentThread().getName() + " " + e);
            }
        }
        if (setting.log.equals("DEBUG")){
            logger.info("Thread: " + Thread.currentThread().getName() + " Closing function spam()");
        }
    }

    public static void printStatistics(Statistics statistics){
        System.out.println("RequestsPerSeconds: " + statistics.requestsPerSeconds);
        System.out.println("AverageDelay: " + statistics.averageDelay + " millis");
        System.out.println("Errors: " + statistics.errors + "%");
        System.out.println("Threads: " + statistics.threads);
        System.out.println("MaxDelay: " + statistics.maxDelay + " millis");
        System.out.println("Capacity: " + statistics.capacity + " kb/s");
    }
}

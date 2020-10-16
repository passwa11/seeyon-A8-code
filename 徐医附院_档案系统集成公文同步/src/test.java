public class test {
    public static void main(String[] args) {
        System.out.println(Math.round(Math.random()*10));

        String time="2020-06-10 12:12:12";
        String createTime = time.substring(0,time.lastIndexOf(":"));
        System.out.println(createTime);
    }
}

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 周刘成   2019/6/24
 */
public class Test {
    public static void main(String[] args) {
        //时间转字符串格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String dateTime = LocalDateTime.now(ZoneOffset.of("+8")).format(formatter);
        System.out.println(dateTime);
        //字符串转时间
        String dateTimeStr = "2019-06-24 00:00:00";
        String oldTimeStr = "2019-06-23 00:00:00";

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime2 = LocalDateTime.parse(dateTimeStr, df);
        LocalDateTime oldtime = LocalDateTime.parse(oldTimeStr, df);
//        Long second=dateTime2.toEpochSecond(ZoneOffset.of("+8"));
//        System.out.println(second);

        Duration duration = Duration.between(oldtime, LocalDateTime.now());
        Long hour = duration.toHours();
        System.out.println(hour);


    }
}

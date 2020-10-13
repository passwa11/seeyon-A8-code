import com.seeyon.ctp.util.JDBCAgent;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class test {

    public static void main(String[] args) throws SQLException {
        Connection connection= JDBCAgent.getRawConnection();
        PreparedStatement ps=null;
        String sql="update test set name =? where id =?";
        ps=connection.prepareStatement(sql);
        String id="3333";
        ps.setBigDecimal(1, BigDecimal.valueOf(1));
        ps.setBigDecimal(2, BigDecimal.valueOf(Long.parseLong(id)));

        ps.executeUpdate();
    }
}

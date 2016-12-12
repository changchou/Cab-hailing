import entitys.DbConnection;
import entitys.PassengerEntity;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mr.Z on 2016/5/8 0008.
 */
public class TClientHandler extends IoHandlerAdapter {

    private List<IoSession> allSessions = new ArrayList<>();


    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);

        allSessions.add(session);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);

        allSessions.remove(session);
    }


    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);

        String msg = (String) message;

        if (msg.contains("passenger")) {
            //接收到客户信息，数据库保存乘客数据

            Session hiberSession = DbConnection.getSession();
            Transaction transaction = hiberSession.beginTransaction();

            PassengerEntity pe = new PassengerEntity();
            pe.setPassengerInfo(msg);

            hiberSession.save(pe);
            transaction.commit();
            hiberSession.close();

        } else if (msg.contains("taxi")) {
            //接收到司机信息，广播出去

            for (IoSession ioSession : allSessions) {
                ioSession.write(message);
            }

        } else if (msg.contains("cancel")) {

            //接收到客户取消订单，广播该消息
            for (IoSession ioSession : allSessions) {
                ioSession.write(message);
            }

            //删除数据库数据
            String[] tempC = msg.split("@");
            int hiberId;
            //遍历数据库
            Session hiberSession = DbConnection.getSession();
            List<PassengerEntity> list = hiberSession.createCriteria(PassengerEntity.class).addOrder(Order.desc("id")).list();
            for (PassengerEntity e : list) {

                String p = e.getPassengerInfo();
                String[] tempP = p.split("@");
                //当数据库中乘客名字、目的地、手机号与发送来的乘客名字、目的地、手机号相同时则是该用户取消订单
                //获取该数据的id，在数据库中删除
                if (tempC[1].equals(tempP[1]) && tempC[2].equals(tempP[4]) && tempC[3].equals(tempP[5])) {
                    hiberId = e.getId();

                    //删除数据库数据
                    hiberSession.beginTransaction();
                    PassengerEntity pe = (PassengerEntity) hiberSession.get(PassengerEntity.class, hiberId);
                    if (pe != null) {
                        hiberSession.delete(pe);
                    }

                    hiberSession.getTransaction().commit();

                }


            }
            hiberSession.close();

        } else if (msg.contains("order")) {
            //接收到司机接单信息

            //获取该订单在数据库的Id
            String[] tempO = msg.split("@");
            int hiberId = Integer.parseInt(tempO[4]);
            //通过id获得实例
            Session hiberSession = DbConnection.getSession();
            hiberSession.beginTransaction();
            PassengerEntity pe = (PassengerEntity) hiberSession.get(PassengerEntity.class, hiberId);

            if (pe != null) {
                //存在该项表示订单存在
                //将接单消息广播出去，发送到客户端触发接单信息，发送到其他司机端更新订单信息
                for (IoSession ioSession : allSessions) {
                    if (!(ioSession.equals(session))) {
                        ioSession.write(message);
                    }
                }
                //删除服务器订单数据
                hiberSession.delete(pe);
            }else {
                //如果不存在该数据，表示该订单已被抢单
                session.write("you are late");
            }

            hiberSession.getTransaction().commit();
            hiberSession.close();
        }


        //发布数据库的客户信息
        Session hiberSession = DbConnection.getSession();
        List<PassengerEntity> list = hiberSession.createCriteria(PassengerEntity.class).addOrder(Order.asc("id")).list();
        for (PassengerEntity e : list) {

            String passengerInfo = e.getPassengerInfo() + "@" + e.getId();

            for (IoSession ioSession : allSessions) {
                ioSession.write(passengerInfo);
            }

        }
        hiberSession.close();

    }
}

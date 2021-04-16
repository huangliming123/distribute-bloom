package im.cu.api.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * Created by huangliming on 2021/3/24
 */
@Slf4j
public class ProtobufUtil {


    @SuppressWarnings({ "unchecked" })
    public static <T> byte[] serializer(Object object) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        // 序列化
        baos = new ByteArrayOutputStream();
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        } finally{
            try {
                oos.close();
                baos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println(e.getMessage());
            }

        }
        return null;
    }

    public static <T> T deserializer(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream bais = null;
        try {
            // 反序列化
            bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object o = ois.readObject();
            return o == null ? null : (T) o;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        } finally{
            try {
                bais.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println(e.getMessage());
            }

        }
        return null;
    }
}

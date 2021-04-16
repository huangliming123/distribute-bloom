package im.cu.api.utils;

import im.cu.api.monitor.Durating;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by huangliming on 2021/3/22
 */
@Slf4j
public class DumpService {

    @Value("${spring.profiles.active}")
    private String active;

    @Value("${server.port}")
    private int port;

    private static final String DUMP_DIRECT = "/data/cache/";
    private static final String INDEX_EXTENSION = ".index";
    private static final String CONFIG_EXTENSION = ".config";
    private static Map<String, ReentrantLock> lockMap;

    @PostConstruct
    private void init() {
        lockMap = new ConcurrentHashMap<>();
    }

    private static ReentrantLock lock = new ReentrantLock();

    @Durating
    public void dump(Object object, String fileName) {
        fileName = getFileNameByActive(fileName);
        ReentrantLock lock = lockMap.computeIfAbsent(fileName, lk -> new ReentrantLock());
        if (!lock.tryLock()) {
            return;
        }
        try {
            File rootDir = new File(DUMP_DIRECT);
            if (!rootDir.exists()) {
                rootDir.mkdirs();
            }
            String newMd5 = DigestUtils.md5DigestAsHex(object.toString().getBytes());
            File indexFile = new File(DUMP_DIRECT + fileName + INDEX_EXTENSION);
            if (compareMd5Equal(indexFile, newMd5)) {
                return;
            }
            writeIndexFile(indexFile, newMd5);
            writeFile(new File(DUMP_DIRECT + fileName + CONFIG_EXTENSION), object);
        } finally {
            lock.unlock();
        }
    }

    private void writeIndexFile(File indexFile, String newMd5) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(indexFile);
            fileWriter.write(newMd5);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean compareMd5Equal(File indexFile, String newMd5) {
        if (indexFile.exists()) {
            BufferedReader indexReader = null;
            try {
                indexReader = new BufferedReader(new FileReader(indexFile));
                String oldMd5 = indexReader.readLine();
                if (Objects.equals(oldMd5, newMd5)) {
                    return true;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (indexReader != null) {
                    try {
                        indexReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    private void writeFile(File originalFile, Object data) {
        File tmp = new File(originalFile.getAbsolutePath() + ".tmp");
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)));
            oos.writeObject(data);
            oos.flush();
            tmp.renameTo(originalFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Durating
    public Object read(String filename) {
        filename = getFileNameByActive(filename);
        File file = new File(DUMP_DIRECT + filename + CONFIG_EXTENSION);
        if (!file.exists()) {
            return null;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            Object res = ois.readObject();
            return res;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private String getFileNameByActive(String fileName) {
        return fileName + (active.equals("dev") ? ("-" + port) : "");
    }
}

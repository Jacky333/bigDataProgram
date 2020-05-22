package com.pm.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author pengcheng
 * @version V1.0
 * @description
 * @date 2020/04/09 20:02
 */
public class HdfsTest {
    private static final String HDFS_PATH = "hdfs://192.168.147.133:9000";
    private static final String HDFS_USER = "root";
    private static FileSystem fileSystem;

    @Before
    public void prepare() {
        try {
            Configuration configuration = new Configuration();
            // 这里我启动的是单节点的 Hadoop,所以副本系数设置为 1,默认值为 3
            configuration.set("dfs.replication", "2");
            fileSystem = FileSystem.get(new URI(HDFS_PATH), configuration, HDFS_USER);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    @After
    public void destroy() {
        fileSystem = null;
    }

    /**
     * 创建目录,支持递归创建目录
     *
     * @throws Exception
     */
    @Test
    public void mkDir() throws Exception {
        fileSystem.mkdirs(new Path("/hdfs-api/test0/"));
    }

    /**
     * 创建指定权限的目录
     * FsPermission(FsAction u, FsAction g, FsAction o) 的三个参数分别对应：创建者权限，同组其他用户权限，其他用户权限，权限值定义在 FsAction 枚举类中。
     *
     * @throws Exception
     */
    @Test
    public void mkDirWithPermission() throws Exception {
        fileSystem.mkdirs(new Path("/hdfs-api/test1/"),
                new FsPermission(FsAction.READ_WRITE, FsAction.READ, FsAction.READ));
    }

    /**
     * 创建文件，并写入内容
     *
     * @throws Exception
     */
    @Test
    public void create() throws Exception {
        // 如果文件存在，默认会覆盖, 可以通过第二个参数进行控制。第三个参数可以控制使用缓冲区的大小
        FSDataOutputStream out = fileSystem.create(new Path("/hdfs-api/test/a.txt"), true, 4096);
        out.write("hello hadoop!".getBytes());
        out.write("hello spark!".getBytes());
        out.write("hello flink!".getBytes());
        // 强制将缓冲区中内容刷出
        out.flush();
        out.close();
    }

    /**
     * 判断文件是否存在
     *
     * @throws Exception
     */
    @Test
    public void exist() throws Exception {
        boolean exists = fileSystem.exists(new Path("/hdfs-api/test/a.txt"));
        System.out.println(exists);
    }

    /**
     * 查看文件内容
     *
     * @throws Exception
     */
    @Test
    public void readToString() throws Exception {
        FSDataInputStream inputStream = fileSystem.open(new Path("/hdfs-api/test/a.txt"));
        String context = inputStreamToString(inputStream, "utf-8");
        System.out.println(context);
    }

    /**
     * 把输入流转换为指定编码的字符
     *
     * @param inputStream 输入流
     * @param encode      指定编码类型
     */
    private static String inputStreamToString(InputStream inputStream, String encode) {
        try {
            if (encode == null || ("".equals(encode))) {
                encode = "utf-8";
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encode));
            StringBuilder builder = new StringBuilder();
            String str = "";
            while ((str = reader.readLine()) != null) {
                builder.append(str).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件重命名
     *
     * @throws Exception
     */
    @Test
    public void rename() throws Exception {
        Path oldPath = new Path("/hdfs-api/test/a.txt");
        Path newPath = new Path("/hdfs-api/test/b.txt");
        boolean result = fileSystem.rename(oldPath, newPath);
        System.out.println(result);
    }

    /**
     * 删除目录或文件
     * @throws Exception
     */
    @Test
    public void delete() throws Exception {
        /*
         *  第二个参数代表是否递归删除
         *    +  如果 path 是一个目录且递归删除为 true, 则删除该目录及其中所有文件;
         *    +  如果 path 是一个目录但递归删除为 false,则会则抛出异常。
         */
        boolean result = fileSystem.delete(new Path("/hdfs-api/test/b.txt"), true);
        System.out.println(result);
    }


    /**
     * 上传文件到HDFS
     * @throws Exception
     */
    @Test
    public void copyFromLocalFile() throws Exception {
        // 如果指定的是目录，则会把目录及其中的文件都复制到指定目录下
        Path src = new Path("G:\\bigData\\OBS-Studio-23.1-Full-Installer-x64.exe");
        Path dst = new Path("/hdfs-api/test/");
        fileSystem.copyFromLocalFile(src, dst);
    }

    /**
     * 上传大文件并显示上传进度
     * @throws Exception
     */
    @Test
    public void copyFromLocalBigFile() throws Exception {

        File file = new File("G:\\bigData\\mariadb-10.3.8-winx64.msi");
        final float fileSize = file.length();
        InputStream in = new BufferedInputStream(new FileInputStream(file));

        FSDataOutputStream out = fileSystem.create(new Path("/hdfs-api/test/mariadb-10.3.8-winx64.msi"),
                new Progressable() {
                    long fileCount = 0;

                    public void progress() {
                        fileCount++;
                        // progress 方法每上传大约 64KB 的数据后就会被调用一次
                        System.out.println("上传进度：" + (fileCount * 64 * 1024 / fileSize) * 100 + " %");
                    }
                });

        IOUtils.copyBytes(in, out, 4096);

    }

    /**
     * 从HDFS上下载文件
     * @throws Exception
     */
    @Test
    public void copyToLocalFile() throws Exception {
        Path src = new Path("/hdfs-api/test/mariadb-10.3.8-winx64.msi");
        Path dst = new Path("G:\\bigData\\aa\\");
        /*
         * 第一个参数控制下载完成后是否删除源文件,默认是 true,即删除;
         * 最后一个参数表示是否将 RawLocalFileSystem 用作本地文件系统;
         * RawLocalFileSystem 默认为 false,通常情况下可以不设置,
         * 但如果你在执行时候抛出 NullPointerException 异常,则代表你的文件系统与程序可能存在不兼容的情况 (window 下常见),
         * 此时可以将 RawLocalFileSystem 设置为 true
         */
        fileSystem.copyToLocalFile(false, src, dst, true);
    }

    /**
     * 查看指定目录下所有文件目录的信息
     * @throws Exception
     */
    @Test
    public void listFiles() throws Exception {
        FileStatus[] statuses = fileSystem.listStatus(new Path("/hdfs-api"));
        for (FileStatus fileStatus : statuses) {
            //fileStatus 的 toString 方法被重写过，直接打印可以看到所有信息
            System.out.println(fileStatus.toString());
        }
    }

    /**
     * 递归查看指定目录下所有文件的信息
     * @throws Exception
     */
    @Test
    public void listFilesRecursive() throws Exception {
        RemoteIterator<LocatedFileStatus> files = fileSystem.listFiles(new Path("/hdfs-api"), true);
        while (files.hasNext()) {
            LocatedFileStatus fileStatus = files.next();
            System.out.println(fileStatus);
            System.out.println(fileStatus.getPath().getName());
            BlockLocation[] blockLocations = fileStatus.getBlockLocations();
            for (BlockLocation blockLocation : blockLocations) {
                String[] hosts = blockLocation.getHosts();
                for (String host : hosts) {
                    System.out.println(host);
                }
            }
            System.out.println("----------------------------");
        }
    }

    /**
     * 查看文件的块信息
     * @throws Exception
     */
    @Test
    public void getFileBlockLocations() throws Exception {

        FileStatus fileStatus = fileSystem.getFileStatus(new Path("/hdfs-api/test/mariadb-10.3.8-winx64.msi"));
        BlockLocation[] blocks = fileSystem.getFileBlockLocations(fileStatus, 0, fileStatus.getLen());
        for (BlockLocation block : blocks) {
            System.out.println(block);
        }
    }
}

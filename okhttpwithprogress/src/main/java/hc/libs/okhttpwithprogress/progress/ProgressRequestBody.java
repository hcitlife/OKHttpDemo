package hc.libs.okhttpwithprogress.progress;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;

import hc.libs.okhttpwithprogress.listener.ProgressListener;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 包装的请求体，处理进度
 * 通过构造函数传入，内部维护了一个原始的RequestBody 以及一个监听器。
 * 文件上传是在write函数中进行监听的设置，在write函数中进行了类似的操作，并回调了接口中的函数。当系统
 * 内部调用了RequestBody的writeTo函数时，我们对BufferedSink进行了一层包装，即设置了进度监听，并返回了我们包装的BufferedSink
 */
public  class ProgressRequestBody extends RequestBody {
    //实际的待包装请求体
    private final RequestBody requestBody;
    //进度回调接口
    private final ProgressListener progressListener;
    //包装完成的BufferedSink
    private BufferedSink bufferedSink;

    /**
     * 构造函数，赋值
     * @param requestBody 待包装的请求体
     * @param progressListener 回调接口
     */
    public ProgressRequestBody(RequestBody requestBody, ProgressListener progressListener) {
        this.requestBody = requestBody;
        this.progressListener = progressListener;
    }

    /**
     * 重写调用实际的响应体的contentType
     * @return MediaType
     */
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    /**
     * 重写调用实际的响应体的contentLength
     * @return contentLength
     * @throws IOException 异常
     */
    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    /**
     * 重写进行写入
     * @param sink BufferedSink
     * @throws IOException 异常
     */
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));            //包装
        }
        requestBody.writeTo(bufferedSink);        //写入
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();

    }

    /**
     * 写入，回调进度接口
     * @param sink Sink
     * @return Sink
     */
    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数
            long bytesWritten = 0L;
            //总字节长度，避免多次调用contentLength()方法
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength();
                }
                //增加当前写入的字节数
                bytesWritten += byteCount;
                //回调
                if (progressListener!=null) {
                    progressListener.onProgress(bytesWritten, contentLength, bytesWritten == contentLength);
                }
            }
        };
    }
}
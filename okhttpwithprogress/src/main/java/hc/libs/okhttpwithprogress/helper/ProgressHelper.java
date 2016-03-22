package hc.libs.okhttpwithprogress.helper;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import hc.libs.okhttpwithprogress.listener.ProgressListener;
import hc.libs.okhttpwithprogress.progress.ProgressRequestBody;
import hc.libs.okhttpwithprogress.progress.ProgressResponseBody;


/**
 * 进度回调辅助类，用来对上传或者下载进行监听设置。
 * 文件的上传：将原始RequestBody和监听器传入，返回我们自定义的包装的ProgressRequestBody，使用包装后的ProgressRequestBody进行请求即可
 * 文件的下载：OkHttp给我们返回的是Response，需要通过拦截器将我们包装的ProgressResponseBody设置进去
 *
 * 对于文件下载的监听器我们为了不影响原来的OkHttpClient 实例，我们调用clone方法进行了克隆，之后对克隆的方法设置了响应拦截，
 * 并返回该克隆的实例。而文件的上传则十分简单，直接包装后返回即可。
 */
public class ProgressHelper {
    /**
     * 包装OkHttpClient，用于下载文件的回调
     * @param client 待包装的OkHttpClient
     * @param progressListener 进度回调接口
     * @return 包装后的OkHttpClient，使用clone方法返回
     */
    public static OkHttpClient addProgressResponseListener(OkHttpClient client,final ProgressListener progressListener){
        //克隆
        OkHttpClient clone = client.clone();
        //增加拦截器
        clone.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                //拦截
                Response originalResponse = chain.proceed(chain.request());
                //包装响应体并返回
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        });
        return clone;
    }

    /**
     * 包装请求体用于上传文件的回调
     * @param requestBody 请求体RequestBody
     * @param progressRequestListener 进度回调接口
     * @return 包装后的进度回调请求体
     */
    public static ProgressRequestBody addProgressRequestListener(RequestBody requestBody,ProgressListener progressRequestListener){
        //包装请求体
        return new ProgressRequestBody(requestBody,progressRequestListener);
    }
}

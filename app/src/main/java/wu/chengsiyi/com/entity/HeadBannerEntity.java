package wu.chengsiyi.com.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ${Wu} on 2018/3/1.
 */

public class HeadBannerEntity implements Serializable {

    private List<ImagesUrlBean> images_url;

    public List<ImagesUrlBean> getImages_url() {
        return images_url;
    }

    public void setImages_url(List<ImagesUrlBean> images_url) {
        this.images_url = images_url;
    }

    public static class ImagesUrlBean {
        /**
         * link : http://sxadv.oss-cn-shenzhen.aliyuncs.com/DSY/Public/Upload/image/20171220/1513730306283839.jpg
         * connect_url : http://www.yihisxmini.com/productview/125.html
         */

        private String link;
        private String connect_url;

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getConnect_url() {
            return connect_url;
        }

        public void setConnect_url(String connect_url) {
            this.connect_url = connect_url;
        }
    }
}

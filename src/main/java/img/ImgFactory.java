package img;

import java.awt.*;

public class ImgFactory {
    public Img create(String path, Dimension size, boolean keepAspect) {
        return new BuffImg().read(path, size, keepAspect, null);
    }
}

package o;

import com.aba.ibank.model.responses.BaseResponse;
import java.util.ArrayList;

public final class CustomTabsSession2 extends BaseResponse {
    public static final int $stable = 8;
    @NotificationCompatExtras(ICustomTabsCallback = "l_name")
    public ArrayList<String> suggestions;
    @NotificationCompatExtras(ICustomTabsCallback = "username")
    public String username = "";
}

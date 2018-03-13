package Modules;

import java.util.List;

/**
 * Created by Giuseppe on 21/03/2017.
 */

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
    void onDirectionFinderError();
}

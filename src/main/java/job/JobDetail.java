package job;

import java.io.Serial;
import java.io.Serializable;

public class JobDetail implements Serializable {
    @Serial
    private static final long serialVersionUID = -2643656014709961191L;
    private final String detail;
    private final int detailWeighting;

    //constructor
    public JobDetail(String detail, int detailWeighting) {
        this.detail = detail;
        this.detailWeighting = detailWeighting;
    }

    //function to override toString for printing
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(detail);
        s.append(" - ");
        s.append(detailWeighting);
        return s.toString();
    }

    //getter functions

    public String getDetail() {
        return detail;
    }

    public int getDetailWeighting() {
        return detailWeighting;
    }
}


package clus.main.settings;

public class EnumTest {

    public enum Day {
        SUNDAY("mcsundae"),
        MONDAY("mandi"),
        TUESDAY("tuzdej"),
        WEDNESDAY("vednsdej"),
        THURSDAY("trzdej"),
        FRIDAY("frajdej"),
        SATURDAY("caturday");

        private String name;


        Day(String txt) {
            name = txt;
        }


        public String getValue() {
            return name;
        }
    }

    public enum Month {
        JAN(1),
        FEB(2),
        MAR(3),
        APR(4),
        MAY(5),
        JUN(6),
        JUL(7),
        AUG(8),
        SEP(9),
        OCT(10),
        NOV(11),
        DEC(12);

        private int month;


        Month(int monthNum) {
            this.month = monthNum;
        }


        public int getValue() {
            return month;
        }
    }


    public static void main(String[] args) {
        Month m = Month.APR;
        System.out.println(m + " has value " + m.getValue());

        Day d = Day.SUNDAY;
        System.out.println(d + " has value " + d.getValue());

    }
}

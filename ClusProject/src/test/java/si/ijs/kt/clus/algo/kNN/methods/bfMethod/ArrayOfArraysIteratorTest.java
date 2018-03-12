package si.ijs.kt.clus.algo.kNN.methods.bfMethod;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import si.ijs.kt.clus.BaseTestCase;
import si.ijs.kt.clus.util.tuple.Pair;

public class ArrayOfArraysIteratorTest extends BaseTestCase {

  @Test
  public void allEmptyTest() {
    Integer[][] empty1 = new Integer[0][];
    Integer[][] empty2 = new Integer[][] {new Integer[0]};
    Integer[][] empty3 = new Integer[][] {new Integer[0], new Integer[0]};
    Integer[][] empty4 = new Integer[][] {new Integer[0], new Integer[0], new Integer[0]};

    ArrayList<Integer> solution = new ArrayList<Integer>();

    Integer[][][] examples = new Integer[][][] {empty1, empty2, empty3, empty4};
    for (Integer[][] example : examples) {
      ArrayOfArraysIterator<Integer> iter = new ArrayOfArraysIterator<Integer>(example);
      ArrayList<Integer> result = new ArrayList<Integer>();
      for (Integer i : iter) {
        result.add(i);
      }
      assertTrue(result.equals(solution));
    }
  }

  @Test
  public void singletonTest() {
    Integer[][] test1 = new Integer[][] {new Integer[] {21}};
    ArrayList<Integer> solution1 = new ArrayList<Integer>(Arrays.asList(21));
    Integer[][] test2 = new Integer[][] {new Integer[] {21, 12, 1, -1}};
    ArrayList<Integer> solution2 = new ArrayList<Integer>(Arrays.asList(21, 12, 1, -1));

    @SuppressWarnings("unchecked")
    Pair<Integer[][], ArrayList<Integer>>[] pairs =
        new Pair[] {
          new Pair<Integer[][], ArrayList<Integer>>(test1, solution1),
          new Pair<Integer[][], ArrayList<Integer>>(test2, solution2)
        };

    for (Pair<Integer[][], ArrayList<Integer>> pair : pairs) {
      ArrayOfArraysIterator<Integer> iter = new ArrayOfArraysIterator<Integer>(pair.getFirst());
      ArrayList<Integer> result = new ArrayList<Integer>();
      for (Integer i : iter) {
        result.add(i);
      }
      assertTrue(result.equals(pair.getSecond()));
    }
  }

  @Test
  public void mixedTest() {
    Integer[][] test1 =
        new Integer[][] {new Integer[] {}, new Integer[] {1}, new Integer[] {}, new Integer[] {3}};
    ArrayList<Integer> solution1 = new ArrayList<Integer>(Arrays.asList(1, 3));
    Integer[][] test2 =
        new Integer[][] {new Integer[] {1}, new Integer[] {}, new Integer[] {3}, new Integer[] {}};
    ArrayList<Integer> solution2 = new ArrayList<Integer>(Arrays.asList(1, 3));
    Integer[][] test3 =
        new Integer[][] {new Integer[] {}, new Integer[] {1}, new Integer[] {3}, new Integer[] {}};
    ArrayList<Integer> solution3 = new ArrayList<Integer>(Arrays.asList(1, 3));
    Integer[][] test4 =
        new Integer[][] {new Integer[] {1}, new Integer[] {}, new Integer[] {}, new Integer[] {3}};
    ArrayList<Integer> solution4 = new ArrayList<Integer>(Arrays.asList(1, 3));
    Integer[][] test5 =
        new Integer[][] {
          new Integer[] {}, new Integer[] {1, 2, 3}, new Integer[] {}, new Integer[] {3}
        };
    ArrayList<Integer> solution5 = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 3));
    Integer[][] test6 = new Integer[][] {new Integer[] {1, 2, 3}, new Integer[] {3, -5, 3}};
    ArrayList<Integer> solution6 = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 3, -5, 3));

    @SuppressWarnings("unchecked")
    Pair<Integer[][], ArrayList<Integer>>[] pairs =
        new Pair[] {
          new Pair<Integer[][], ArrayList<Integer>>(test1, solution1),
          new Pair<Integer[][], ArrayList<Integer>>(test2, solution2),
          new Pair<Integer[][], ArrayList<Integer>>(test3, solution3),
          new Pair<Integer[][], ArrayList<Integer>>(test4, solution4),
          new Pair<Integer[][], ArrayList<Integer>>(test5, solution5),
          new Pair<Integer[][], ArrayList<Integer>>(test6, solution6)
        };

    for (Pair<Integer[][], ArrayList<Integer>> pair : pairs) {
      ArrayOfArraysIterator<Integer> iter = new ArrayOfArraysIterator<Integer>(pair.getFirst());
      ArrayList<Integer> result = new ArrayList<Integer>();
      for (Integer i : iter) {
        result.add(i);
      }
      assertTrue(result.equals(pair.getSecond()));
    }
  }
}

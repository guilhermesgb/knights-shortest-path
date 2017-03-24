import java.io.*;
import java.util.*;

public class Horse {

   public static class Position {

       public final int x;
       public final int y;

       public Position(int x, int y) {
           this.x = x;
           this.y = y;
       }

   }

   public static void main(String[] args) {
       Scanner in = new Scanner(System.in);
       int initialX, initialY, targetX, targetY, dimensions;
       initialX = in.nextInt();
       initialY = in.nextInt();
       targetX = in.nextInt();
       targetY = in.nextInt();
       dimensions = in.nextInt();

       Position initial = new Position(initialX, initialY);
       Position target = new Position(targetX, targetY);
       solve(initial, target, dimensions);
   }

   private static void solve(Position initial, Position target, int dimensions) {
       boolean initialWithin3By3 = Math.abs(target.x - initial.x) <= 2 && Math.abs(target.y - initial.y) <= 2;
       if (dimensions <= 2) {
           //For any matrix with dimensions lower than 3, there's no possible solution since the horse won't be able to move around.
           System.out.println(0);
       } else if (dimensions == 3 || initialWithin3By3) {
           //We handle the case where matrix is 3x3 (or where there's a submatrix 3x3 containing both initial and target) with precomputed results.
           System.out.println(evaluate3By3(initial, target, dimensions));
       } else {
           //Apply simulation to solve for matrices with dimensions bigger than 3x3.
           System.out.println(simulateForBiggerThan3By3(initial, target, dimensions));
       }
   }

   private static int evaluate3By3(Position initial, Position target, int dimensions) {
       //First of all, we'll compute the 3x3 grid that includes both initial and target, in case the matrix is not a 3x3 matrix.
       //Otherwise we just hardcode the corners to be (0,0) and (2,2) and center will thus be (1,1).
       Position topLeftCorner = dimensions != 3 ? (target.x <= initial.x && target.y <= initial.y ? target : initial) : new Position(0, 0);
       Position bottomRightCorner = new Position(topLeftCorner.x + 2, topLeftCorner.y + 2);
       if (bottomRightCorner.x >= dimensions) {
           topLeftCorner = new Position(topLeftCorner.x - 1, topLeftCorner.y);
           bottomRightCorner = new Position(bottomRightCorner.x - 1, bottomRightCorner.y);
       }
       if (bottomRightCorner.y >= dimensions) {
           topLeftCorner = new Position(topLeftCorner.x, topLeftCorner.y - 1);
           bottomRightCorner = new Position(bottomRightCorner.x, bottomRightCorner.y - 1);
       }
       Position center = new Position((bottomRightCorner.x + topLeftCorner.x) / 2, (bottomRightCorner.y + topLeftCorner.y) / 2);
       if ((target.x == center.x && target.y == center.y) || (initial.x == center.x && initial.y == center.y)) {
           //Handle (I.) case: considering the 3x3 matrix that includes them, either initial or target are in the middle.
           //In this case, if matrix is really 3x3, then solution is 0. Otherwise, it depends. If initial is a lateral neighbor,
           //then solution is 3. Otherwise, initial is a diagonal neighbor, and if either initial or target are in the corner of the matrix,
           //solution is 4. Solution is 2, otherwise.
           return dimensions == 3 ? 0 : (isLateralNeighbor(initial, target) ? 3 :
               (matrixIsCornered(initial, target, topLeftCorner, bottomRightCorner, dimensions) ? 4 : 2));
       } else if ((target.x == initial.x && target.x == center.x && Math.abs(target.y - initial.y) == 2)
               || (target.y == initial.y && target.y == center.y && Math.abs(target.x - initial.x) == 2)) {
           //Handle (II.) case: initial and target are both in the middle line or the middle column, with one empty slot between them.
           return dimensions == 3 ? 4 : 2;
       } else {
           //Handle (III.) case: the distance between initial and target determines the amount of steps required for initial to reach target:
//Distance equals 1? Solution is 3; Distance equals 2? Solution is 2; Distance equals 3? Solution is 1; Distance equals 4? Solution is 4.
           switch (distance(initial, target)) {
               case 1:
                   return 3;
               case 2:
                   return 2;
               case 3:
                   return 1;
               case 4:
                   return 4;
           }
       }
       return -1;
   }

   private static boolean matrixIsCornered(Position initial, Position target, Position topLeftCorner, Position bottomRightCorner, int dimensions) {
       //Checks whether either initial or target are located at one of the four corners of the matrix.
       for (Position toBeConsidered : new Position[] { initial, target }) {
           if ((toBeConsidered.x == topLeftCorner.x && toBeConsidered.y == topLeftCorner.y && topLeftCorner.x == 0)
                   || (toBeConsidered.x == topLeftCorner.x + 2 && toBeConsidered.y == topLeftCorner.y && topLeftCorner.x + 2 == dimensions - 1)
                   || (toBeConsidered.x == bottomRightCorner.x && toBeConsidered.y == bottomRightCorner.y && bottomRightCorner.x == dimensions - 1)
                   || (toBeConsidered.x == bottomRightCorner.x - 2 && toBeConsidered.y == bottomRightCorner.y && bottomRightCorner.x - 2 == 0)) {
               return true;
           }
       }
       return false;
   }

   private static int simulateForBiggerThan3By3(Position initial, Position target, int dimensions) {
       //We'll simulate the horse's step by step movement blindly towards the target.
       //It will move one step at a time, following the chess' rules for horse movement,
       //choosing to go towards the position that brings it a bit closer to the target.
       //If many such positions exist, one of them is picked at random.
       //The simulation shall stop once the horse reaches either the target itself,
       //or one of the target's direct neighbors (lateral or diagonal).
       int stepsTaken = 0;
       while (!hasReached(initial, target)) {
           initial = nextMove(initial, target, dimensions);
           stepsTaken += 1;
       }
       if (isLateralNeighbor(initial, target)) {
           stepsTaken += 1;
       } else if (isDiagonalNeighbor(initial, target)) {
           stepsTaken += 2;
       }
       return stepsTaken;
   }

   private static boolean hasReached(Position initial, Position target) {
       //Checks whether initial is exactly at target or that it is a direct neighbor.
       return initial.x == target.x && initial.y == target.y
           || isLateralNeighbor(initial, target) || isDiagonalNeighbor(initial, target);
   }

   private static boolean isLateralNeighbor(Position initial, Position target) {
       //Checks whether initial is a lateral (that is, a non-diagonal) neighbor of target.
       return (target.y == initial.y && (target.x == initial.x - 1 || target.x == initial.x + 1))
           || (target.x == initial.x && (target.y == initial.y - 1 || target.y == initial.y + 1));
   }

   private static boolean isDiagonalNeighbor(Position initial, Position target) {
       //Checks whether initial is a diagonal neighbor of target.
       return (target.y == initial.y - 1 && (target.x == initial.x - 1 || target.x == initial.x + 1))
           || (target.y == initial.y + 1 && (target.x == initial.x - 1 || target.x == initial.x + 1));
   }

   private static Position nextMove(Position initial, final Position target, int dimensions) {
       //Calculates the best move for the horse to advance blindly toward the target.
       //Possible moves sit at vectors: (1,2), (2,1), (2,-1), (1,-2), (-1,-2), (-2,-1), (-2,1), (-1,2).
       //The best move is the one that brings the horse closer to the target.
       //Some random move is chosen among the best if there's a tie.
       PriorityQueue<Position> moves = new PriorityQueue<Position>(8, new Comparator<Position>() {

           @Override
           public int compare(Position position, Position anotherPosition) {
               int distanceFromPositionToTarget = distance(position, target);
               int distanceFromAnotherPositionToTarget = distance(anotherPosition, target);
               return distanceFromPositionToTarget - distanceFromAnotherPositionToTarget;
           }

       });
       Position[] vectors = new Position[] {
           new Position( 1,  2), new Position( 2,  1),
           new Position( 2, -1), new Position( 1, -2),
           new Position(-1, -2), new Position(-2, -1),
           new Position(-2,  1), new Position(-1,  2)
       };
       for (Position vector : vectors) {
           Position move = new Position(initial.x + vector.x, initial.y + vector.y);
           if (isMoveAllowed(move, dimensions)) {
               moves.add(move);
           }
       }
       return moves.remove();
   }

   private static boolean isMoveAllowed(Position position, int dimensions) {
       //Checks whether move is allowed, that is, the move will place the horse
       //at a new position that is still within the boundaries of the matrix.
       return position.x >= 0 && position.x <= dimensions - 1
           && position.y >= 0 && position.y <= dimensions - 1;
   }

   private static int distance(Position position, Position anotherPosition) {
       //Calculates how many horizontal or vertical jumps are needed to go from position to anotherPosition.
       return Math.abs(position.x - anotherPosition.x) + Math.abs(position.y - anotherPosition.y);
   }

}

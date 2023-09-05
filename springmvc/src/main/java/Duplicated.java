import java.util.Arrays;

public class Duplicated {

    // 1~5 사이의 랜덤한 난수를 10개 발생 시켜 배령레 저장하고 그 배열 속에서 중복 된 값을 제거해라
    // ex) [1,1,1,3,3,3,5,5,5,2,2,] 처럼 배열이 있다면 [1,3,5,2]로 만들어 보아라 0은 제외
    public static void main(String[] args) {

        // 10개의 난수를 저장 받을 변수를 생성
        int[] arr = new int[10];

        // 1~5 까지의 난수 10개를 0 부터 9 인텓스에 저장 받고
        for (int i = 0; i < arr.length; i++) { //arr = 10개 난수를 품고 있다.
            arr[i] = (int)(Math.random() * 5) + 1;
        }

        // 값이 잘 들어갔나 확인 10개의 난수가 저장 되어있어야 한다.
        System.out.println(Arrays.toString(arr));

        // 중복이 배제된 값을 저장받을 배열
        int[] temp = new int[5];

        // 중복이 배제된 회수
        int count = 0;

        // 중복을 걸러주는 부분
        for(int i = 0; i < arr.length; i++){
            boolean flag = false;
            for(int j = 0; j < temp.length; j++){
                if(arr[i] == temp[j]){
                    flag = true;
                    System.out.println(Arrays.toString(temp));
                }
            }

            // 중복이 배제된 값이 저장
            if(!flag){ // false 가 아니면, i = j 이면, 0을 넣는다.
                temp[count++] = arr[i];
            }
        }

        // 0을 배제 해주기 위한 부분
        int[] result = new int[count];
        for(int i = 0; i < result.length; i++){
            result[i] = temp[i];
        }
        System.out.println(Arrays.toString(result));
    }
}

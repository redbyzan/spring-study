import adapter.Person;
import adapter.PersonAdapter;
import builder.Text;
import factory.Factory;
import factory.IdCardFactory;
import factory.Product;
import observer.DigitObserver;
import observer.GraphicObserver;
import observer.Observer;
import observer.RandomGenerator;
import singleton.Singleton;
import strategy.PaperStrategy;
import strategy.Player;
import strategy.RockStrategy;

public class Main {

    public static void main(String[] args) {
        // builder
        Text text = new Text.Builder()
                .setTitle("title")
                .setContent("content")
                .setItems("항목1", "항목2", "항목3")
                .build();

        System.out.println("text = " + text);

        System.out.println("========================");

        // factory
        Factory factory = new IdCardFactory();
        Product idCard1 = factory.createProduct("홍길동");
        Product idCard2 = factory.createProduct("한석봉");

        idCard1.use();
        idCard2.use();

        System.out.println("========================");

        // singleton
        Singleton instance1 = Singleton.getInstance();
        Singleton instance2 = Singleton.getInstance();
        if (instance1 == instance2){
            System.out.println("일치");
        } else{
            System.out.println("불일치");
        }

        System.out.println("========================");

        // adapter
        Person person = new Person("홍길동", 25);
        PersonAdapter adapter = new PersonAdapter(person);
        System.out.println("adapter.showName() = " + adapter.showName());
        System.out.println("adapter.showAge() = " + adapter.showAge());

        System.out.println("========================");

        // observer
        RandomGenerator randomGenerator = new RandomGenerator();
        Observer observer = new DigitObserver();
        randomGenerator.addObserver(observer);
        randomGenerator.execute();

        // strategy
        Player player1 = new Player("player1", new RockStrategy());
        Player player2 = new Player("player2", new PaperStrategy());
        System.out.println(player1.nextHand().fight(player2.nextHand()));

    }
}


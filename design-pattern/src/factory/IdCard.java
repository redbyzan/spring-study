package factory;

public class IdCard implements Product {

    private String owner;

    public IdCard(String owner) {
        this.owner = owner;
    }

    @Override
    public void use() {
        System.out.println(owner+ " 님의 카드를 사용합니다.");
    }
}

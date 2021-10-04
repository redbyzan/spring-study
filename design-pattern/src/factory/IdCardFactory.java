package factory;

public class IdCardFactory implements Factory{
    @Override
    public IdCard createProduct(String name) {
        return new IdCard(name);
    }
}

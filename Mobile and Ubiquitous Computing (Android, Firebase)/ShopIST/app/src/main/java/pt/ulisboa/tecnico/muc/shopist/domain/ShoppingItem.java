package pt.ulisboa.tecnico.muc.shopist.domain;

public class ShoppingItem extends DomainComparator {

  private Shopping shopping;
  private Float price;

  public ShoppingItem(Shopping shopping) {
    super(shopping.getCrowdShoppingId(), shopping.getTimestamp());
    this.shopping = shopping;
  }

  public ShoppingItem(Shopping shopping, Float price) {
    super(shopping.getCrowdShoppingId(), shopping.getTimestamp());
    this.shopping = shopping;
    this.price = price;
  }

  public ShoppingItem(String crowdShoppingId, Float price) {
    super(crowdShoppingId, null);
    this.shopping = null;
    this.price = price;
  }

  public ShoppingItem(ShoppingItem shoppingItem) {
    super(shoppingItem.getId(), shoppingItem.getTimestamp());
    this.shopping = shoppingItem.getShopping();
    this.price = shoppingItem.getPrice();
  }

  public Shopping getShopping() {
    return this.shopping;
  }

  public void setShopping(Shopping shopping) {
    this.shopping = shopping;
    setTimestamp(shopping.getTimestamp());
  }

  public Float getPrice() {
    return price;
  }

  public void setPrice(Float price) {
    this.price = price;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ShoppingItem)) return false;
    return this.getId().equals(((ShoppingItem) o).getId());
  }

  @Override
  public int hashCode() {
    return this.getId().hashCode();
  }
}

package pt.ulisboa.tecnico.muc.shopist.domain;

public class Cart {

  private String shoppingId;
  private int totalInCart;

  public Cart() {
    this.shoppingId = null;
    this.totalInCart = 0;
  }

  public Cart(String shoppingId, int totalInCart) {
    this.shoppingId = shoppingId;
    this.totalInCart = totalInCart;
  }

  public String getShoppingId() {
    return this.shoppingId;
  }

  public void setShoppingId(String shoppingId) {
    this.shoppingId = shoppingId;
  }

  public int getTotalInCart() {
    return this.totalInCart;
  }

  public void addToCart(int toAdd) {
    this.totalInCart += toAdd;
  }

  public void removeFromCart(int toRemove) {
    this.totalInCart -= toRemove;
  }

  public boolean hasCart() {
    return this.totalInCart > 0;
  }

  public void clear() {
    this.shoppingId = null;
    this.totalInCart = 0;
  }
}

package mall.shopping.mall.service.cart;

import mall.shopping.mall.domain.Cart;
import mall.shopping.mall.domain.CartItem;
import mall.shopping.mall.domain.Product;
import mall.shopping.mall.domain.User;
import mall.shopping.mall.repository.CartItemRepository;
import mall.shopping.mall.repository.CartRepository;
import mall.shopping.mall.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.DecimalFormat;
import java.util.Optional;

@Service
public class CartService {


    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartRepository cartRepository, ProductRepository productRepository, CartItemRepository cartItemRepository1) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository1;
    }

    // cartId를 통해 장바구니 조회 (특정 유저의 카트도 조회 가능)
    public Cart getCart(Long cartId) {
        if (cartId == null) {
            throw new IllegalArgumentException("카트의 ID가 존재하지 않습니다 NULL");
        }

        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    // 사용자의 장바구니를 가져오는 메소드
    public Cart getCartByUser(User user) {
        // 사용자의 장바구니를 찾기 위한 쿼리
        Cart cart = cartRepository.findByUserId(user.getId());

        // 사용자가 장바구니를 가지고 있지 않다면, 새로 생성해서 반환
        if (cart == null) {
            cart = createNewCart(user); // 새로운 카트 생성 (createNewCart 메소드가 필요)
        }

        return cart;
    }

    // 새로운 카트를 생성
    public Cart createNewCart(User user) {
        // 새로운 Cart 객체 생성 후 사용자와 연결
        Cart cart = new Cart();
        cart.setUser(user); // 카트에 사용자 설정
        cartRepository.save(cart); // DB에 저장
        return cart;
    }

    //통합 값
    public String getFormattedTotalPrice(Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        // 카트의 총 금액 계산
        int totalPrice = cart.getItems().stream()
                .mapToInt(CartItem::getTotalPrice) // 각 아이템의 총 금액을 더함
                .sum();

        // 숫자 포맷 (천 단위 구분 쉼표 추가)
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(totalPrice); // 포맷된 문자열 반환
    }


    /*
    List (Needed to ADD Features )
     : 장바구니 추가
     : 문제점 장바구니의 Order ID 와 Product ID 등등 문제 발생

    List :1 사용자의 장바구니 찾기
         :2 장바구니가 존재하지 않는다면 장바구니 생성
         :3 장바구니의 해당 상품이 있는지 확인
         :4 장바구니의 상품이 없으면 새로운 Cart_Item 생성


         -> Session 으로 관련된 장바구니 시스템 구현하기 위해선 로그인 기능 필요
         >> 장바구니 개발하다가 Login으로 개발 옮김 -> 11월 28일 ->>> 11월 30일 Login 기능 완 (oAuth 기능 추가 필요 )

         1. 사용자의 장바구니 확인:
         2. 장바구니가 없으면 생성:
         3. 장바구니에 상품이 있는지 확인:
         4. 상품이 있으면 수량 업데이트:
         5. 상품이 없으면 새로 추가:
*/

    @Transactional
    public void addProductToCart(User user, Product newproduct, int quantity) {

        // 1. 장바구니 확인
        Cart cart = cartRepository.findByUserId(user.getId());

        // 2. 장바구니 없으면 생성
        if (cart == null) {
            cart = createNewCart(user);  // 기존의 createCart 대신 createNewCart 사용
        }

        // 3. 장바구니에 CartItem이 있는지 확인
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, newproduct);

        // 4. 상품이 있으면 수량 업데이트:
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
        } else {
            // 장바구니에 정보가 없다면 새로운 정보 저장
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(newproduct);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    // 선택 아이디 지우기
    @Transactional
    public void removeAllItemsFromCart(Cart cart){

        cartItemRepository.deleteAllByCart(cart);
    }


}


package mall.shopping.mall.controller;


import jakarta.servlet.http.HttpSession;
import mall.shopping.mall.domain.Cart;
import mall.shopping.mall.domain.CartItem;
import mall.shopping.mall.domain.Product;
import mall.shopping.mall.domain.User;
import mall.shopping.mall.repository.CartRepository;
import mall.shopping.mall.repository.ProductRepository;
import mall.shopping.mall.service.cart.CartService;
import mall.shopping.mall.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DecimalFormat;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private ProductService productService;


    @GetMapping
    public String viewCart(
            @RequestParam(value = "cartId", required = false) Long cartId, HttpSession httpSession,
            Model model) {

        User user = (User) httpSession.getAttribute("user");

        if (user == null) {
            model.addAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        if (cartId == null||cartId == 0) {
            Cart cart = cartService.getCartByUser(user);
            cartId = cart.getId(); // 기본값으로 사용자의 장바구니 ID 사용
        }

        try {
            // 장바구니 총 금액 계산
            Cart cart = cartService.getCart(cartId);
            String formattedTotalPrice = cartService.getFormattedTotalPrice(cartId);

            model.addAttribute("cart", cart);
            model.addAttribute("formattedTotalPrice", formattedTotalPrice); // 총 금액을 모델에 추가
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Cart ID not found: " + cartId);
            return "error";
        }

        return "cart"; // 정상적인 뷰
    }




    @PostMapping("/add/{id}")
    @ResponseBody
    public String addCartAndProductToCartItem(@PathVariable(name="id") Long id, @RequestParam(name="quantity") int quantity,HttpSession session,Model model){

        User user = (User)session.getAttribute("user");

        if (user == null) {

            return "로그인이 필요합니다.";

        }


        // 상품을 DB에서 조회
        Product product = productService.getProductById(id).orElse(null);

        if (product == null) {

            return "상품을 찾을 수 없습니다.";
        }
        // CartService의 addProductToCart 메서드 호출
        cartService.addProductToCart(user, product, quantity);

        return "장바구니에 추가되었습니다.";

    }

    @PostMapping("/remove/{cartId}")
    public String removeCart(@PathVariable(name = "cartId")Long cartId, HttpSession httpSession){

        System.out.println("Received cartId: " + cartId);

        User user = (User)httpSession.getAttribute("user");

        if(user == null){

            System.out.println("로그인이 필요합니다.");

            return "redirect:/login";
        }

        // cartId 로그 확인
        System.out.println("Received cartId: " + cartId);

        Cart cart = cartService.getCart(cartId);

        if (cart == null) {

            System.out.println("카트가 비어있습니다.");

            return "redirect:/cart";
        }

        // 장바구니 비우기 로직 (장바구니의 모든 상품 제거)
        cartService.removeAllItemsFromCart(cart);

        // 장바구니 페이지로 리다이렉트
        return "redirect:/cart";
    }



}


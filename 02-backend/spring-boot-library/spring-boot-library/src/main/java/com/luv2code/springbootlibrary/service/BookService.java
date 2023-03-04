package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.dao.CheckoutRepository;
import com.luv2code.springbootlibrary.dao.HistoryRepository;
import com.luv2code.springbootlibrary.entity.Book;
import com.luv2code.springbootlibrary.entity.Checkout;
import com.luv2code.springbootlibrary.entity.History;
import com.luv2code.springbootlibrary.responsemodels.ShelfCurrentLoansResponse;
import org.hibernate.annotations.Check;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class BookService {

    private BookRepository bookRepository;
    private CheckoutRepository checkoutRepository;
    private HistoryRepository historyRepository;

    public BookService(BookRepository bookRepository, CheckoutRepository checkoutRepository, HistoryRepository historyRepository){
        this.bookRepository = bookRepository;
        this.checkoutRepository = checkoutRepository;
        this.historyRepository = historyRepository;
    }

    public Book checkoutBook(String userEmail, Long bookId) throws Exception{

        Optional<Book> book = bookRepository.findById(bookId);

        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);

        if (book.isEmpty() || validateCheckout != null || book.get().getCopiesAvailable() <= 0){
            throw new Exception("Book doesn't exist or already checked out by user");
        }

        book.get().setCopiesAvailable(book.get().getCopiesAvailable() - 1);
        bookRepository.save(book.get());

        Checkout checkout = new Checkout(userEmail, LocalDate.now().toString(), LocalDate.now().plusDays(7).toString(),book.get().getId());

        checkoutRepository.save(checkout);

        return book.get();
    }

    public boolean checkoutBookByUser(String userEmail, Long bookId){
        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);

        return validateCheckout != null;
    }

    public int currentLoadsCount(String userEmail){
        return checkoutRepository.findCheckoutsByUserEmail(userEmail).size();
    }

    public List<ShelfCurrentLoansResponse> currentLoans(String userEmail) throws Exception{
        List<ShelfCurrentLoansResponse> shelfCurrentLoansResponses = new ArrayList<>();
        List<Checkout> checkoutList = checkoutRepository.findCheckoutsByUserEmail(userEmail);

        List<Long> bookIdList = new ArrayList<>();

        for(Checkout i : checkoutList){
            bookIdList.add(i.getBookId());
        }

        List<Book> books = bookRepository.findBooksByBookIds(bookIdList);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for(Book book : books){
            Optional<Checkout> checkout =  checkoutList.stream()
                    .filter(x-> x.getBookId().equals(book.getId()))
                    .findFirst();
            if (checkout.isPresent()){
                Date d1 = simpleDateFormat.parse(checkout.get().getReturnDate());
                Date d2 = simpleDateFormat.parse(LocalDate.now().toString());

                TimeUnit time = TimeUnit.DAYS;

                long differenceInTime = time.convert(d1.getTime() - d2.getTime(), TimeUnit.MILLISECONDS);
                shelfCurrentLoansResponses.add(new ShelfCurrentLoansResponse(book, (int)differenceInTime));
            }
        }
        return shelfCurrentLoansResponses;
    }

    public void returnBook(String userEmail, Long bookId) throws Exception{
        Optional<Book> book = bookRepository.findById(bookId);

        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);

        if(!book.isPresent() || validateCheckout == null){
            throw new Exception("Book doesn't exist or not checked out by user");
        }

        book.get().setCopiesAvailable(book.get().getCopiesAvailable() + 1);
        bookRepository.save(book.get());

        checkoutRepository.deleteById(validateCheckout.getId());
        History history = new History(
                userEmail,
                validateCheckout.getCheckoutDate(),
                LocalDate.now().toString(),
                book.get().getTitle(),
                book.get().getAuthor(),
                book.get().getDescription(),
                book.get().getImg()
        );

        historyRepository.save(history);
    }

    public void renewLoan(String userEmail, Long bookId) throws Exception{
        Checkout checkout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);

        if (checkout==null){
            throw new Exception("book does not exist or not checked out by user");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date d1 = simpleDateFormat.parse(checkout.getReturnDate());
        Date d2 = simpleDateFormat.parse(LocalDate.now().toString());

        if(d1.compareTo(d2) >= 0){
            checkout.setReturnDate(LocalDate.now().plusDays(14).toString());
            checkoutRepository.save(checkout);
        }
    }
}

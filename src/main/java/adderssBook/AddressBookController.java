package adderssBook;

import filesSystem.FileManager;
import filesSystem.FileManagerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AddressBookController {

    static final String PATH = "./src/main/resources/";
    FileManager fileManager;
    private Map<String, AddressBook> addressBooks;
    public AddressBookController() {
        this.fileManager = new FileManagerFactory().getFileManager();
        this.addressBooks = new LinkedHashMap<>();
    }

    public boolean createNewBook(String bookName) throws AddressBookException {
        File newBook = new File(PATH+bookName+".json");
        try {
            boolean isBookCreated = newBook.createNewFile();
            if (!isBookCreated)
                throw new AddressBookException("Existing book with name "+bookName, AddressBookException.ExceptionType.DUPLICATE_NAME);
            fileManager.writeIntoFile(newBook, new AddressBook(bookName));
        } catch (IOException e) {
            throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.FILE_PROBLEM);
        }
        return true;
    }

    public int loadAddressBook(String bookName) throws AddressBookException {
        try {
            AddressBook book = fileManager.readFile(PATH + bookName + ".json", AddressBook.class);
            addressBooks.put(book.bookName, book);
            return book.personsData.size();
        } catch (FileNotFoundException e) {
            throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.NO_ADDRESS_BOOK_FOUND);
        } catch (IOException e) {
            throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.FILE_PROBLEM);
        }
    }

    public List<PersonDTO> displayBook(String bookName) throws AddressBookException {
        if (addressBooks == null || addressBooks.size() == 0)
            throw new AddressBookException("no address book loaded", AddressBookException.ExceptionType.NO_DATA);
        List<PersonDTO> personDTOS = addressBooks.get(bookName).personsData.values()
                .stream()
                .map(PersonDTO::new)
                .collect(Collectors.toList());
        Comparator<PersonDTO> comparatorWithName = Comparator.comparing(personDTO -> personDTO.firstName);
        Comparator<PersonDTO> comparatorWithZip = comparatorWithName.thenComparing(personDTO -> personDTO.address.zip);
        personDTOS.sort(comparatorWithZip);
        return personDTOS;
    }

    public void addNewData(String bookName, String firstName, String lastName, String phoneNo,
                           String city, String state, String zip) {
        addressBooks.get(bookName).addNewPerson(new PersonInfo(firstName, lastName,phoneNo,new Address(city,state,zip)));
    }

    public void save(String bookName) {
        fileManager.writeIntoFile(PATH+bookName+".json", addressBooks.get(bookName));
    }

    public PersonDTO getPersonDetails(String bookName, String phoneNo) {
        return new PersonDTO(addressBooks.get(bookName).personsData.get(phoneNo));
    }

    public void edit(String bookName, String phoneNo, PersonDTO personDTO) {
        addressBooks.get(bookName).editPersonInfo(phoneNo, personDTO);
    }

    public void delete(String bookName) {
        addressBooks.remove(bookName);
        try {
            Files.delete(Paths.get(PATH+bookName+".json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete(String bookName, String phoneNo) {
        addressBooks.get(bookName).remove(phoneNo);
    }

    public void saveAs(String bookName,String newBookName) throws AddressBookException, IOException {
        Files.copy(Paths.get(PATH+bookName+".json"), Paths.get(PATH+newBookName+".json"));
        addressBooks.put(newBookName, new AddressBook(newBookName, addressBooks.get(bookName)));
        save(newBookName);
    }

}












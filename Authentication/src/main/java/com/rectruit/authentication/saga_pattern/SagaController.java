package com.rectruit.authentication.saga_pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/saga")
public class SagaController {

    @Autowired

    private TransactionService transactionService;

    @PostMapping("/rollback")
    public ResponseEntity<?> rollbackSaga(@RequestBody String requestBody) {
        try {
            JSONObject data = new JSONObject(requestBody);
            String transactionId = data.getString("id");
            transactionService.saga_fail(transactionId);
            return ResponseEntity.status(HttpStatus.OK).body("success");
        } catch (JSONException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON data");
        }
//        catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction data not found");
//        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @PostMapping("/success")
    public ResponseEntity<?> successSaga(@RequestBody String requestBody) {
        try {
            JSONObject data = new JSONObject(requestBody);
            String transactionId = data.getString("id");
            transactionService.saga_success(transactionId);
            return ResponseEntity.status(HttpStatus.OK).body("success");
        } catch (JSONException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON data");
        }
//        catch (TransactionLogNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction data not found");
//        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }
}

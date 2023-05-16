/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aagarciga.jlox;

import static com.aagarciga.jlox.TokenType.*;
import java.util.List;

/**
 *
 * @author alexalvarezgarciga
 */
public class Parser {
    private static class ParseError extends RuntimeException {}
    
    private final List<Token> tokens;
    private int current = 0;
    
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    Expression parse(){
        try{
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }
    
    private Expression expression(){
        return equality();
    }
    
    // equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expression equality(){
        Expression expr = comparison();
        
        while(match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expr = new Expression.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    //comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expression comparison(){
        Expression expr = term();
        
        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expression right = term();
            expr = new Expression.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    private Expression term(){
        Expression expr = factor();
        
        while(match(MINUS, PLUS)){
            Token operator = previous();
            Expression right = factor();
            expr = new Expression.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    private Expression factor(){
        Expression expr = unary();
        
        while(match(SLASH, STAR)){
            Token operator = previous();
            Expression right = unary();
            expr = new Expression.Binary(expr, operator, right);
        }
        
        return expr;
    }
    
    // unary          → ( "!" | "-" ) unary
    //                  | primary ;
    
    private Expression unary(){
        if(match(BANG, MINUS)){
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }
        
        return primary();
    }
    
    // primary        → NUMBER | STRING | "true" | "false" | "nil"
    //                  | "(" expression ")" ;
    private Expression primary(){
        if(match(FALSE)) return new Expression.Literal(false);
        if(match(TRUE)) return new Expression.Literal(true);
        if(match(NIL)) return new Expression.Literal(null);
        
        if(match(NUMBER, STRING)) {
            return new Expression.Literal(previous().literal);
        }
        
        if(match(LEFT_PAREN)){
            Expression expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expr);
        }
        
        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for(TokenType type: types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }
    
    private Token consume(TokenType type, String message){
        if(check(type)) return advance();
        
        throw error(peek(), message);
    }
    
    private boolean check(TokenType type) {
        if(isAtEnd())
            return false;
        return peek().type == type;
    }
    
    private Token advance() {
        if(!isAtEnd())
            current++;
        return previous();
    }
    
    private boolean isAtEnd(){
        return peek().type == EOF;
    }
    
    private Token peek() {
        return tokens.get(current);
    }
    
    private Token previous(){
        return tokens.get(current - 1);
    }    
    
    private ParseError error(Token token, String message){
        Jlox.error(token, message);
        return new ParseError();
    }
    
    private void synchronize(){
        advance();
        
        while(!isAtEnd()) {
            if(previous().type == SEMICOLON) 
                return;
            
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            
            advance();
        }
        
    }
    
}



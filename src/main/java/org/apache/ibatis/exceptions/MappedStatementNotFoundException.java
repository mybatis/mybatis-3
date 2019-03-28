package org.apache.ibatis.exceptions;

public class MappedStatementNotFoundException extends IbatisException{

    private static final long serialVersionUID = 5538331948275113168L;

    public MappedStatementNotFoundException() {
        super();
    }

    public MappedStatementNotFoundException(String message) {
        super(message);
    }

}

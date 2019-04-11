define ([], function () {

    return function (data, view) {

        $(view).w2popup ('open', {

            width  : 280,
            height : 250,

            title   : 'Смена пароля',

            onOpen: function (e) {

                e.done (function () {               
                
                    $('#w2ui-popup .w2ui-form').w2reform ({

                        name: 'passwordForm',

                        record: {},

                        fields : [
                            { name: 'p1', type: 'password'},
                            { name: 'p2', type: 'password'},
                        ],
                        
                    })
                    
                    clickOn ($('.w2ui-buttons button[name=update]'), $_DO.update_user_password)

                })
                
            }
            
        })
    
    }
    
});
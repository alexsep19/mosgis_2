define ([], function () {

    return function (data, view) {
    
        var name = 'bank_account_rokr_terminate_popup_form'
        
        $(view).w2popup('open', {

            width  : 320,
            height : 170,

            title   : 'Закрытие счёта РОКР',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'closedate',  type: 'date'},
                        ],
                                                
                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_bank_account_rokr_terminate_popup)

                }

            }

        });

    }

});
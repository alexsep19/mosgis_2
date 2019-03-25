define ([], function () {

    return function (data, view) {
    
        var name = 'rc_contract_annul_popup_form'
        
        $(view).w2popup('open', {

            width  : 580,
            height : 240,

            title   : 'Аннулирование договора',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'reason_of_annulment',  type: 'text'},
                        ],
                                                
                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_rc_contract_annul_popup)

                }

            }

        });

    }

});
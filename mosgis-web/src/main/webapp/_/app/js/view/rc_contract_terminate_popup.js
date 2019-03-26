define ([], function () {

    return function (data, view) {
    
        var name = 'rc_contract_terminate_popup_form'
        
        $(view).w2popup('open', {

            width  : 580,
            height : 240,

            title   : 'Расторжение договора',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'date_of_termination',  type: 'date'},
                            {name: 'reason_of_termination', type: 'list', options: {items: data.vc_nsi_54}}
                        ],
                                                
                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_rc_contract_terminate_popup)

                }

            }

        });

    }

});
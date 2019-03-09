define ([], function () {

    return function (data, view) {    
        
        $(fill (view, {})).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'bank_acct_select_popup_form',

                record: data.record,

                fields : [
                    {name: 'uuid_bnk_acct', type: 'list', options: {items: data.bnk_accts_actual}},
                ],
                
                focus: -1,

            })

       })

    }

})
define ([], function () {

    return function (data, view) {
        
        $(view).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'annul_popup_form',

                record: data.record,

                fields : [                
                
                    {name: 'terminationdate', type: 'date', options: {end: data.record.terminationdate}},
                    {name: 'annulmentinfo', type: 'text'},
                    {name: 'code_vc_nsi_330', type: 'list', options: {
                        items: data.vc_nsi_330.items
                    }},                
                ],
                
                focus: 1,

            })

       })

    }

})
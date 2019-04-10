define ([], function () {

    return function (data, view) {
        
        $(view).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'overhaul_short_program_house_works_popup_form',

                record: data.record,

                fields : [                
                    {name: 'work', type: 'list', options: {items: data.vc_oh_wk_types.items}},
                    {name: 'endmonth', type: 'text'},
                    {name: 'endyear', type: 'text'},
                    {name: 'fund', type: 'text'},
                    {name: 'regionbudget', type: 'text'},
                    {name: 'municipalbudget', type: 'text'},
                    {name: 'owners', type: 'text'}
                ],
                
            })

       })

    }

})
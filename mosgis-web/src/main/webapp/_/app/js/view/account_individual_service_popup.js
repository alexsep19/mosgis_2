define ([], function () {

    var name = 'account_individual_service_popup_form'

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: name,

                record: data.record,

                fields : [                                
                
                    {name: 'begindate', type: 'date', options: {
                        keyboard: false,
                        start:    data.dt_from,
                    }},
                    {name: 'enddate',   type: 'date', options: {
                        keyboard: false,
                        end:      data.dt_to,
                    }},
                    
                    
                    {name: 'uuid_add_service', type: 'list', options: {items: data.add_services}},

                    {name: 'files', type: 'file', options: {max: 1}},                    
                    
                ],
                
                focus: -1,
                                                        
            })

       })

    }

})
define ([], function () {

    var name = 'voc_user_form'

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: name,

                record: data.record,

                fields : [                                
                
                    {name: 'begindate', type: 'date', options: {
                        keyboard: false,
//                        start:    dt_dmy (data.item.date_),
                    }},
                    {name: 'enddate',   type: 'date', options: {
                        keyboard: false,
//                        start:    dt_dmy (data.item.date_),
                    }},
                    
                    
                    {name: 'uuid_add_service', type: 'list', options: {items: []}},

                    {name: 'files', type: 'file', options: {max: 1}},                    
                    
                ],
                                                        
            })

       })

    }

})
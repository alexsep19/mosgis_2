define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'org_work_form',

                record: data.record,

                fields : [          
                ],

            })
            
            var is_virgin = 1
            
            $('#required_services_container').w2regrid ({ 
            
                name: 'account_items_grid',
                
                show: {
                    toolbar: false,
                    footer: false,
                    selectColumn: true
                },     
                
                columns: [
                    {field: 'no', caption: '№ ЛС', size: 20},
                    {field: 'label', caption: 'Собственник', size: 50},
                ],
                
                records: data.account_items,
                
                onRender: function (e) {
                    
                    var grid = this
                
                    e.done (function () {
                        $.each (data.accs, function () {grid.select (this.id)})                        
                    })
                
                }                
            
            }).refresh ()

       })

    }

})
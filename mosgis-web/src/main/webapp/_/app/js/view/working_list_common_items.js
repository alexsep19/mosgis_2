define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'working_list_common_items_grid',

            show: {
                toolbar: true,
                toolbarAdd: true,
                toolbarInput: false,
                footer: true,
            },     
            
            columns: [  
            
                {field: 'price', caption: 'Цена', size: 10},
                
            ],
            
            records: [],
            
            onAdd: function () {
                use.block ('working_list_common_items_popup')
            }

        }).refresh ();

    }

})
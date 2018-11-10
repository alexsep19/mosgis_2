define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')
        
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'mgmt_contract_common_log',

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

        }).refresh ();

    }

})
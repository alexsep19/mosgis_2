define ([], function () {

    return function (data, view) {
    
        var it = data.item       
            
        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'metering_device_common_accounts_grid',

            show: {
                toolbar: true,
                toolbarAdd: it._can.edit,
                toolbarInput: false,
                footer: true,
            },     

            columns: [                
                {field: 'acc.accountnumber', caption: '№ ЛС', size: 20},
                {field: 'label', caption: 'Собственник', size: 50},
            ],
            
            records: data.accs,
            
            onAdd: $_DO.create_metering_device_common_accounts,
            
        }).refresh ();

    }

})
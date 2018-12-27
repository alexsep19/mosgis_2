define ([], function () {

    return function (data, view) {
    
        data = $('body').data ('data')

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: 'license_common_houses',

            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },
            
            columns: [                
                {field: 'fias.label', caption: 'Адрес',    size: 100},

            ],
            
            url: '/mosgis/_rest/?type=licenses&part=houses&id=' + $_REQUEST.id,            

        }).refresh ();

    }

})
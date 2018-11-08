define ([], function () {
    
    var grid_name = 'house_voting_protocols_grid'
    
    function getData () {
        return $('body').data ('data')
    }
            
    return function (data, view) {

        console.log (data)
        
        

        var layout = w2ui ['topmost_layout']
        
        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: true,
                toolbarColumns: true,
                toolbarAdd: true,
                toolbarDelete: true,
            },            

            textSearch: 'contains',

            columns: 
            [                
                {field: 'protocolnum', caption: 'Номер протокола', size: 10, hidden: 1},
                {field: 'protocoldate', caption: 'Дата составления протокола', size: 7, render: _dt},
                {field: 'extravoting', caption: 'Вид собрания', size: 7, render: function (r) {return r.extravoting ? 'Внеочередное' : 'Ежегодное'}},
                {field: 'meetingeligibility', caption: 'Правомочность проведения собрания', size: 10, render: function (r) {return r.meetingeligibility == "C" ? 'Правомочное' : 'Неправомочное'}},
                {field: 'modification', caption: 'Основания изменения', size: 20},
                {field: 'form_', caption: 'Форма проведения', size: 15, render: function (r) {
                        switch (r.form_) {
                            case 0:
                                return 'Заочное голосование (опросным путем)'
                            case 1:
                                return 'Очное голосование'
                            case 2:
                                return 'Заочное голосование с использованием системы'
                            case 3:
                                return 'Очно-заочное голосование'
                        }
                }},
            ].filter (not_off),
            
            postData: {data: {"uuid_house": $_REQUEST.id}},

            url: '/mosgis/_rest/?type=voting_protocols',
           
            onDelete: $_DO.delete_house_voting_protocols,
            
            onAdd: $_DO.create_house_voting_protocols,
            
        })

    }
    
})
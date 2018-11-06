define ([], function () {
    
    var grid_name = 'house_voting_protocols_grid'
    
    function getData () {
        return $('body').data ('data')
    }
            
    return function (data, view) {

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({ 
        
            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: true,
                toolbarColumns: true,
                toolbarReload: false,
                toolbarInput: false,
                toolbarAdd: true,
                toolbarDelete: true,
                toolbarEdit: true,
            },            

            textSearch: 'contains',

            columns: [                
                {field: 'protocolnum', caption: 'Номер протокола', size: 10},
                {field: 'protocoldate', caption: 'Дата составления протокола', size: 7, render: _dt},
                
                {field: 'extravoting', caption: 'Вид собрания', size: 7, render: function (r) {return r.extravoting ? 'Внеочередное' : 'Ежегодное'}},
                {field: 'meetingeligibility', caption: 'Правомочность проведения собрания', size: 7, render: function (r) {return r.meetingeligibility == "C" ? 'Правомочное' : 'Неправомочное'}},
                {field: 'modification', caption: 'Основания изменения', size: 20},
                
                {field: 'avotingdate', caption: 'Дата окончания приема решений', size: 7, render: _dt, hidden: 1},
                {field: 'resolutionplace', caption: 'Место приема решения', size: 20, hidden: 1},

                {field: 'meetingdate', caption: 'Дата и время проведения собрания', size: 5, hidden: 1},
                {field: 'votingplace', caption: 'Место проведения собрания', size: 20, hidden: 1},

                {field: 'evotingdatebegin', caption: 'Дата начала проведения голосования', size: 5, render: _dt, hidden: 1},
                {field: 'evotingdateend', caption: 'Дата окончания проведения голосования', size: 5, render: _dt, hidden: 1},
                {field: 'discipline', caption: 'Порядок приема оформленных в письменной форме решений собственников', size: 20, hidden: 1},
                {field: 'inforeview', caption: 'Порядок ознакомления с информацией', size: 20, hidden: 1},

                {field: 'meeting_av_date', caption: 'Дата и время проведения собрания', size: 5, hidden: 1},
                {field: 'meeting_av_date_end', caption: 'Дата окончания приема решений', size: 5, render: _dt, hidden: 1},
                {field: 'meeting_av_place', caption: 'Место проведения собрания', size: 20, hidden: 1},
                {field: 'meeting_av_res_place', caption: 'Место приема решения', size: 20, hidden: 1},
            ].filter (not_off),
            
            postData: {data: {"uuid_house": $_REQUEST.id}},

            url: '/mosgis/_rest/?type=voting_protocols',
           
            onDelete: $_DO.delete_house_voting_protocols,
            
            onAdd: $_DO.create_house_voting_protocols,
            
            onEdit: $_DO.edit_house_voting_protocols,
            
        })

    }
    
})